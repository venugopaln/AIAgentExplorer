package com.medplus.aiagent.service.impl;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.medplus.aiagent.util.SQLQueryProcessor;

import dev.langchain4j.model.anthropic.AnthropicChatModel;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.ollama.OllamaChatModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.model.vertexai.VertexAiChatModel;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.service.SystemMessage;

@Service
public class AIAgentService {
	
	@Value("${com.agent.anthropic.api.key}")
	private String anthropicApiKey;

	public String getQueryByAgentString(String promptInput, String AIType) {
		
		if("OpenAi".equalsIgnoreCase(AIType)) {
			return getQueryByOpenAi(promptInput);
		}else if("Gemini".equalsIgnoreCase(AIType)) {
			return getQueryByGemini(promptInput);
		}else if("anthropic".equalsIgnoreCase(AIType)) {
			return getQueryByAnthropic(promptInput);
		}else if("ollama".equalsIgnoreCase(AIType)) {
			return getQueryByOllama(promptInput);
		}
		return "";
	}

	public static String getFinalOutput(String finalOutput) {
		finalOutput = finalOutput.replaceAll("table_data_sale_detail", "tbl_sale_detail").replaceAll("table_data_sale", "tbl_sale_header")
				.replaceAll("table_itemname", "tbl_product").replaceAll("table_location", "tbl_store");
		/*
		 * if(!(finalOutput.contains("limit") || finalOutput.contains("LIMIT"))) {
		 * if(finalOutput.contains(";")) {
		 * System.out.println("Replacing Semicolumn with limit"); finalOutput =
		 * finalOutput.replaceAll(";", " LIMIT 1000"); }else {
		 * System.out.println("addding limit"); finalOutput = finalOutput+" LIMIT 1000";
		 * } }
		 */
		System.out.println("Before clean Query :"+finalOutput);
		finalOutput = SQLQueryProcessor.cleanAndFormatAIQuery(finalOutput);
		return finalOutput;
	}
	
	private String getQueryByOllama(String promptInput) {
		System.out.println("In ollama promptInput "+promptInput);
		ChatLanguageModel model = OllamaChatModel.builder().baseUrl("http://localhost:11434").modelName("qwen3-coder:30b") //llama3:8b,gemma,duckdb-nsql:7b,qwen2.5-coder:1.5b,tinyllama:1.1b,qwen2.5-coder:7b,qwen3:8b,qwen3-coder:30b,qwen2.5-coder:32b
				.build();

		String note = getDefaultNote();
		String message = getSaleContext()+", "+note+", Input :"+promptInput;
		String answer = model.generate(message);
		System.out.println(answer);
		
		return getFinalOutput(answer);
	}

	private String getDefaultNote() {
		return "You are a SQL generator. Given a natural language request, output only the corresponding SQL query. Do not include explanations or formatting.,Use always context available fields only don't use select *";
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
					  .apiKey(anthropicApiKey)
					  .modelName("claude-sonnet-4-20250514")
					  .temperature(0.7)
					  .build();
			  
			  Assistant assistant = AiServices.create(Assistant.class, model);
			  String note = getDefaultNote();
			  String message = getSaleContext()+", "+note+", Input :"+promptInput;
			  response = assistant.chat(message);
			
			  System.out.println("response :"+response);
		  } catch (Exception e) {
			e.printStackTrace();
		  }
		  return getFinalOutput(response);
		 
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