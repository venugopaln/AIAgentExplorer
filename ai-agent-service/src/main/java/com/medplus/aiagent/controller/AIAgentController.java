package com.medplus.aiagent.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.anthropic.client.AnthropicClient;
import com.anthropic.client.okhttp.AnthropicOkHttpClient;
import com.anthropic.models.messages.Message;
import com.anthropic.models.messages.MessageCreateParams;
import com.anthropic.models.messages.Model;
import com.medplus.aiagent.dao.AIAgentQueryDao;
import com.medplus.aiagent.service.impl.AIAgentService;

@RestController
@RequestMapping("/api/aiagent")
public class AIAgentController {

	@Autowired
	AIAgentService aiAgentService;
	
    @Autowired
    private AIAgentQueryDao aiAgentQueryDao;

    @GetMapping("/input")
    public List<Map<String, Object>> executeQuery(@RequestParam String inputPrompt) {
    	List<Map<String, Object>> output=null;
		try {
			System.out.println("inputPrompt :"+inputPrompt);
			//System.out.println("Model "+Model.CLAUDE_SONNET_4_20250514);
			//String finalQuery = aiAgentService.getQueryByAgentString(inputPrompt, "anthropic");
			String finalQuery = getQueryByAgent(inputPrompt);
			System.out.println("finalQuery :"+finalQuery);
			output = aiAgentQueryDao.executeQuery(finalQuery);
			System.out.println("Output :"+output);
		} catch (Exception e) {
			e.printStackTrace();
			return new ArrayList<Map<String,Object>>();
		}
        return output;
    }

	private String getQueryByAgent(String inputPrompt) {
        AnthropicClient client = AnthropicOkHttpClient.builder().fromEnv().apiKey("").build();

        MessageCreateParams.Builder createParamsBuilder = MessageCreateParams.builder()
                .model(Model.CLAUDE_SONNET_4_20250514)
                .maxTokens(1024)
                .system("You are a SQL generator. Given a natural language request, output only the corresponding SQL query. Do not include explanations or formatting.")
               
                //.addUserMessage(getWhmsContext())
                .addUserMessage(getSaleContext())
                .addUserMessage( "Note: You are a SQL generator. Given a natural language request, output only the corresponding SQL query. Do not include explanations or formatting.,Use always context available fields only don't use select *, Input : "+inputPrompt);

            Message message = client.messages().create(createParamsBuilder.build());
            //System.out.println("message :{}"+new Gson().toJson(message));
          //  System.out.println("message.content() :"+message.content());
            StringBuilder aiOutput = new StringBuilder();
            message.content().stream()
                    .flatMap(contentBlock -> contentBlock.text().stream())
                    .forEach(textBlock -> aiOutput.append(textBlock.text()));
            String finalOutput = aiOutput.toString();
            System.out.println("finalOutput before replace:"+finalOutput);
            finalOutput = finalOutput.replaceAll("table_data_sale_detail", "tbl_sale_detail").replaceAll("table_data_sale", "tbl_sale_header")
            		.replaceAll("table_itemname", "tbl_product").replaceAll("table_location", "tbl_store");
            if(!(finalOutput.contains("limit") || finalOutput.contains("LIMIT"))) {
            	if(finalOutput.contains(";")) {
            		System.out.println("Replacing Semicolumn with limit");
            		finalOutput  = finalOutput.replaceAll(";", " LIMIT 1000");
            	}else {
            		System.out.println("addding limit");
            		finalOutput = finalOutput+" LIMIT 1000";
            	}
            }
            return finalOutput;
		
	}

	private String getSaleContext() {
		String saleContext = "CREATE TABLE `table_data_sale` (\n"
				+ "  `InvoiceID` int NOT NULL AUTO_INCREMENT,\n"
				+ "  `DateCreated` datetime NOT NULL,\n"
				+ "  `StoreID` varchar(12) NOT NULL,\n"
				+ "  `CustomerID` int NOT NULL,\n"
				+ "  `GrandTotal` decimal(10,2) NOT NULL,\n"
				+ "  PRIMARY KEY (`InvoiceID`)\n"
				+ ") ENGINE=InnoDB  DEFAULT CHARSET=latin1;\n"
				+ "\n"
				+ "CREATE TABLE `table_data_sale_detail` (\n"
				+ "  `InvoiceID` int NOT NULL,\n"
				+ "  `ProductID` varchar(8) NOT NULL,\n"
				+ "  `Quantity` decimal(12,2) NOT NULL,\n"
				+ "  `Price` decimal(14,6) NOT NULL,\n"
				+ "  PRIMARY KEY (`InvoiceID`,`ProductID`)\n"
				+ ") ENGINE=InnoDB DEFAULT CHARSET=latin1;\n"
				+ "\n"
				+ "CREATE TABLE `table_itemname` (\n"
				+ "  `ID` mediumint unsigned NOT NULL AUTO_INCREMENT,\n"
				+ "  `ProductID` varchar(8) NOT NULL DEFAULT '',\n"
				+ "  `Name` varchar(200) NOT NULL DEFAULT '',\n"
				+ "  `ManufacturerID` int DEFAULT NULL,\n"
				+ "  `ManufacturerDivisionID` int DEFAULT NULL,\n"
				+ "  PRIMARY KEY (`ID`)\n"
				+ ") ENGINE=InnoDB DEFAULT CHARSET=latin1;\n"
				+ "\n"
				+ "CREATE TABLE `table_location` (\n"
				+ "  `ID` int NOT NULL AUTO_INCREMENT,\n"
				+ "  `StoreID` varchar(12) NOT NULL DEFAULT '',\n"
				+ "  `Name` varchar(50) DEFAULT NULL,\n"
				+ "  PRIMARY KEY (`ID`)\n"
				+ ") ENGINE=InnoDB DEFAULT CHARSET=latin1;\n"
				+ "";
		return saleContext;
	}

	private String getWhmsContext() {
		return "Context: table structure CREATE TABLE `tbl_whms_stock_detail` (\n"
				+ "  `StockDetailId` bigint NOT NULL AUTO_INCREMENT,\n"
				+ "  `StoreId` varchar(12) NOT NULL,\n"
				+ "  `AreaCode` char(1) NOT NULL,\n"
				+ "  `ReferenceID` bigint NOT NULL,\n"
				+ "  `ReferenceType` char(1) NOT NULL,\n"
				+ "  `BatchId` bigint NOT NULL,\n"
				+ "  `Barcode` bigint NOT NULL,\n"
				+ "  `BoxId` bigint NOT NULL,\n"
				+ "  `Quantity` double NOT NULL,\n"
				+ "  `SysTime` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,\n"
				+ "  PRIMARY KEY (`StockDetailId`)\n"
				+ ") ENGINE=InnoDB AUTO_INCREMENT=9 DEFAULT CHARSET=latin1;\n";
	}
	
}