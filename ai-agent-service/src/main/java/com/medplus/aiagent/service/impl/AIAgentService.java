package com.medplus.aiagent.service.impl;

import org.springframework.stereotype.Service;

import dev.langchain4j.model.anthropic.AnthropicChatModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.model.vertexai.VertexAiChatModel;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.service.SystemMessage;

@Service
public class AIAgentService {

	public String getQueryByAgentString(String promptInput, String AIType) {
		
		if("OpenAi".equalsIgnoreCase(AIType)) {
			return getQueryByOpenAi(promptInput);
		}else if("Gemini".equalsIgnoreCase(AIType)) {
			return getQueryByGemini(promptInput);
		}else if("anthropic".equalsIgnoreCase(AIType)) {
			return getQueryByAnthropic(promptInput);
		}
		return "";
	}

	
	  interface Assistant {
	  
		  @SystemMessage("You are a SQL generator. Given a natural language request, output only the corresponding SQL query. Do not include explanations or formatting.,Use always context available fields only don't use select *"
		  ) String chat(String userInput); 
	  }
	 
	  private String getQueryByAnthropic(String promptInput) {
		System.out.println("In anthropic");
	      String response = null;
		  try {
			  AnthropicChatModel model = AnthropicChatModel.builder()
					  .apiKey("")
					  .modelName("claude-sonnet-4-20250514")
					  .temperature(0.7)
					  .build();
			  
			  Assistant assistant = AiServices.create(Assistant.class, model);
			  String note = "You are a SQL generator. Given a natural language request, output only the corresponding SQL query. Do not include explanations or formatting.,Use always context available fields only don't use select *";
			  String message = getSaleContext()+", "+note+", Input :"+promptInput;
			  response = assistant.chat(message);
			
			  System.out.println("response :"+response);
		  } catch (Exception e) {
			e.printStackTrace();
		  }
		  return response;
		 
	}
	
	private String getQueryByOpenAi(String promptInput) {
		
		  String response = null;
		  try {
			OpenAiChatModel model = OpenAiChatModel.builder() .apiKey(
			  "")
					.modelName("gpt-4.1")
				    .temperature(0.7)
			  .build();
			  
			  Assistant assistant = AiServices.create(Assistant.class, model);
			  
			  String message = getSaleContext()+", Input :"+promptInput;
			  response = assistant.chat(message); System.out.println("AI: " + response); 
			  System.out.println("response :"+response);
		  } catch (Exception e) {
			e.printStackTrace();
		  }
		  return response;
		 
	}
	
	private String getQueryByGemini(String promptInput) {
		
		  String response = null;
		  try {
			 
			
			  
			  
			  VertexAiChatModel model = VertexAiChatModel.builder()
		                .project("")
		                .location("asia-south1") // or "europe-west4", etc.
		                .modelName("gemini-1.5-pro") // latest model as of 2025
		                .build();

		        Assistant assistant = AiServices.create(Assistant.class, model);
		        String message = getSaleContext()+", Input :"+promptInput;
		        response = assistant.chat(message);
		        System.out.println("response :"+response);
		        
		  } catch (Exception e) {
			e.printStackTrace();
		  }
		  return response;
		 
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
}