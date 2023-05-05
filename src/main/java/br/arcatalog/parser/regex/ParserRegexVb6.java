package br.arcatalog.parser.regex;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import br.com.arcatalog.model.vb6.FormalParameter;
import br.com.arcatalog.model.vb6.Inventory;
import br.com.arcatalog.model.vb6.InventoryItem;
import br.com.arcatalog.model.vb6.ProcedureDefinition;

public class ParserRegexVb6 {
	ProcedureDefinition procedureDef;
	FormalParameter formalParam;
	Inventory inventory;
	InventoryItem inventoryItem;
	Scanner scanner;

	final String DEFMETHOD_SUB_FUNCTION_REGEX = "^(?:(?i)(?<modifier>\\bPrivate\\b|\\bPublic\\b)\\s+)?(?<procedureType>\\bSub\\b|\\bFunction\\b)\\s+(?<procedureName>\\w[a-zA-Z0-9_]*)\\s*(?<formalParameters>\\((.*)\\))$";
	
	final String FORMAL_PARAMETER_REGEX = "^(?:(?i)(?<name>\\w+)\\s+\\w+\\s+(?<type>\\w+))$";

	final String FUNCTION_CALL_OR_ARRAY_ITEM_OR_STRUCTURE_ITEM_REGEX = "^(?:(?i)(?<name>\\w+[!$%&@#])\\s*(?<realParam>\\(.*\\)))$";

	final String MODULE_DEPENDENCY_REGEX = "^(?:(?i)\\s*(?<dependencyType>\\bObject\\b)\\s*=\\s*(?<Hklm>\"\\{.*?\");\\s*(?<name>\".*\")).*$";

	final String MODULE_HEADER_REGEX = "^(?:(?i)\\s*(?<anchor>\\bVERSION\\b)\\s*(?<version>\\S*)\\s*(?<literal>\\bCLASS\\b)?).*$";

	final String GUI_DEFINITION_REGEX = "^(?:(?i)\\s*(?<anchor>\\bBEGIN\\b)\\s*(?<type>\\S+)\\s*(?<name>\\S+)).*$";

	final String EXPR_REGEX = "^(?:(?i)\\s*(?<expr>[^']+)).*$";
	
	final String UI_BEGINPROPERTY_REGEX = "^(?:(?i)\\s*(?<anchor>\\bBEGINPROPERTY\\b)\\s+(?<attrib>\\w+)\\s+(?<hklm>[^ ]+)).*$";

	final String UI_ENDPROPERTY_REGEX = "^(?:(?i)\\s*(?<anchor>\\bEndProperty\\b)).*$";

	final String OPTION_STMT_REGEX = "^(?:(?i)\\s*(?<anchor>\\bOPTION\\b)\\s*(?<option>[^ ]+)?).*$";

	final String DIRECTIVE_BLOCK_REGEX = "^(?:(?i)\\s*(?<anchor>\\bBEGIN\\b)\\s*(?:'.*)?).*$";
	
	final String COMMAND_STMT = "^(?:(?i)\\s*(?<anchor>\\bAPPACTIVATE\\b|\\bCALL\\b|\\bCHDIR\\b|\\bCLOSE\\b|\\bDOEVENTS\\b|\\bERASE\\b|\\bEXIT\\b|\\bGOSUB\\b|\\bIF\\b|\\bFILECOPY\\b|\\bFOR\\b|\\bEACH\\b|\\bNEXT\\b|\\bGOTO\\b|\\bLINEUNPUT\\b|\\bINPUT\\b|\\bLOAD\\b|\\bKILL\\b|\\bDO\\b|\\bLOOP\\b|\\bMKDIR\\b|\\bNAME\\b|\\bON\\b|\\bOPEN\\b|\\bPUT\\b|\\bRAISE\\b|\\bREDIM\\b|\\bRMDIR\\b|\\bRSET\\b|\\bRESUME\\b|\\bSELECT\\b|\\bCASE\\b|\\bSENDKEYS\\b|\\bSETATTR\\b|\\bSET\\b|\\bSTOP\\b|\\bUNLOAD\\b|\\bWHILE\\b|\\bWITH\\b|\\bWRITE\\b)).*$";

	final String ATTRIBUTE_STMT = "^(?:(?i)\\s*(?<anchor>\bATTRIBUTE\b)\\s*(?<key>\\S+)\\s*=\\s*(?<value>(?:[^\" ]\\S*)|(?:\"[^\"]*\"))).*$";

	final String ATTRIBUTE_SET = "^(?:(?i)\\s*(?<key>\\S+)\\s*=\\s*(?<value>(?:[^\" ]\\S*)|(?:\"[^\"]*\"))).*$";
	
	final String END_BLOCK = "^(?:(?i)\\s*(?<anchor>\\bEND\\b)).*$";

	boolean insideMethod;
	
	ArrayDeque<String> endControlQueue = new ArrayDeque<String>();
	
	String line;
	
	ObjectMapper om = new ObjectMapper();

	
	public ParserRegexVb6() {}
	
	public ParserRegexVb6(String fileName)  {
		System.out.println(fileName);
		
		inventory = new Inventory("VB6");
		try {
			scanner = new Scanner(new File(fileName));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		

		run(fileName);
	}

	public void run(String fileName) {
			while (scanner.hasNextLine()) {
				line = scanner.nextLine();
				if(dependency()) {
					continue;
				}
				if(sub_function()) {
					continue;
				}
				if(module_header()) {
					continue;
				}
				if(gui_definition()) {
					continue;
				}	
				if(end_Block()) {
					continue;
				}
				if(attributeSet()) {
					continue;
				}
			}
			scanner.close();	
	}

	private boolean sub_function() {
		 Pattern p = Pattern.compile(DEFMETHOD_SUB_FUNCTION_REGEX, Pattern.UNICODE_CHARACTER_CLASS);
		 Matcher m = p.matcher(line);
		 if(!m.matches()) return false;
		 m.reset();
		 System.out.println("----------------------------");

		 System.out.println("DEFMETHOD_SUB_FUNCTION_REGEX");
		 while(m.find()) {
			 System.out.println("---------------------------------------------");
			 if(m.start("modifier") > -1) {
				 System.out.println(m.group("modifier"));
			 }
			 if(m.start("procedureType") > -1) {
				 System.out.println(m.group("procedureType"));
			 }
			 if(m.start("formalParameters") > -1) {
				 System.out.println(m.group("formalParameters"));
			 }
			 if(m.start("procedureName") > -1) {
				 System.out.println(m.group("procedureName"));
			 }
		 }
		 return true;
	}
	
	private boolean gui_definition() {
		 Pattern p = Pattern.compile(GUI_DEFINITION_REGEX, Pattern.UNICODE_CHARACTER_CLASS);
		 Matcher m = p.matcher(line);
		 if(!m.matches()) return false;
		 m.reset();
		 System.out.println("----------------------------");

		 System.out.println("GUI_DEFINITION_REGEX");
		 while(m.find()) {
			 System.out.println("---------------------------------------------");
			 
			 if(m.start("anchor") > -1) {
				 System.out.println(m.group("anchor"));
			 }
			 if(m.start("type") > -1) {
				 System.out.println(m.group("type"));
			 }
			 if(m.start("name") > -1) {
				 endControlQueue.push(m.group("name"));
				 System.out.println("Entrando  no bloco: " + m.group("name"));
			 }
		 }
		 return true;
	}	
	
	private boolean end_Block() {
		 Pattern p = Pattern.compile(END_BLOCK, Pattern.UNICODE_CHARACTER_CLASS);
		 Matcher m = p.matcher(line);
		 if(!m.matches()) return false;
		 if(endControlQueue.isEmpty()) return false;
		 m.reset();
		 System.out.println("----------------------------");

		 System.out.println("END_BLOCK");
		 while(m.find()) {
			 System.out.println("---------------------------------------------");
			 if(m.start("anchor") > -1) {
				 if(!endControlQueue.isEmpty()) { // Não é End de bloco begin, mas outro "end"
					 System.out.println("Encerrando o bloco: " + endControlQueue.remove());
				 }
			 }
		 }
		 return true;
	}	

	private boolean module_header() {
		 Pattern p = Pattern.compile(MODULE_HEADER_REGEX, Pattern.UNICODE_CHARACTER_CLASS);
		 Matcher m = p.matcher(line);
		 if(!m.matches()) return false;
		 m.reset();
		 System.out.println("-------------------");

		 System.out.println("MODULE_HEADER_REGEX");
		 while(m.find()) {
			 System.out.println("---------------------------------------------");
			 if(m.start("anchor") > -1) {
				 System.out.println(m.group("anchor"));
			 }
			 if(m.start("version") > -1) {
				 System.out.println(m.group("version"));
			 }
			 if(m.start("literal") > -1) {
				 System.out.println(m.group("literal"));
			 }
		 }
		 return true;
	}

	private boolean attributeSet() {
		 Pattern p = Pattern.compile(ATTRIBUTE_SET, Pattern.UNICODE_CHARACTER_CLASS);
		 Matcher m = p.matcher(line);
		 if(!m.matches()) return false;
		 if(endControlQueue.isEmpty()) return false;
		 m.reset();
		 System.out.println("-------------------");

		 System.out.println("ATTRIBUTE_SET");
		 while(m.find()) {
			 System.out.println("---------------------------------------------");
			 if(m.start("key") > -1) {
				 System.out.println(m.group("key"));
			 }
			 if(m.start("value") > -1) {
				 System.out.println(m.group("value"));
			 }
		 }
		 return true;
	}
	
	private boolean attributeStmt() {
		 Pattern p = Pattern.compile(ATTRIBUTE_STMT, Pattern.UNICODE_CHARACTER_CLASS);
		 Matcher m = p.matcher(line);
		 if(!m.matches()) return false;
		 if(endControlQueue.isEmpty()) return false;
		 m.reset();
		 System.out.println("-------------------");

		 System.out.println("ATTRIBUTE_STMT");
		 while(m.find()) {
			 System.out.println("---------------------------------------------");
			 if(m.start("key") > -1) {
				 System.out.println(m.group("key"));
			 }
			 if(m.start("value") > -1) {
				 System.out.println(m.group("value"));
			 }
		 }
		 return true;
	}
	
	private void splitFormalParameters(String formalParameters, InventoryItem i) {
		formalParameters = formalParameters.replace("(", "");
		formalParameters = formalParameters.replace(")", "");

		String[] formalParametersSplited = formalParameters.split(",");
		if (formalParameters.length() > 0) {
			InventoryItem ii = new InventoryItem("FORMAL_PARAMETER","a");
			for (String param : formalParametersSplited) {
				 Pattern p = Pattern.compile(FORMAL_PARAMETER_REGEX, Pattern.UNICODE_CHARACTER_CLASS);
				 Matcher m = p.matcher(param);
				 Integer iii = 0;
				 while(m.find()) {
					 ii.addDetail(i.toString(), param);
					 iii++;
				 }
			}
			i.addDetail("FORMAL_PARAMETER", ii.toString());
		}
	}
	
//	private boolean function(String line) {
//		 if(insideMethod) return false;
//		 Pattern p = Pattern.compile(DEFMETHOD_FUNCTION_REGEX, Pattern.UNICODE_CHARACTER_CLASS);
//		 Matcher m = p.matcher(line);
//		 if(!m.matches()) return false;
//		 m.reset();
//		 while(m.find()) {
//			 if(m.start("functionName") > -1) {
//				 inventory.add(m.group("functionName"), DEFMETHOD_FUNCTION);
//			 }
//			 if(m.start("formalParameters") > -1) {
//				 formalParameters(m.group("formalParameters"));
//			 }
//		 }
//		 return true;
//	}
	
	private boolean dependency() {
		 Pattern p = Pattern.compile(MODULE_DEPENDENCY_REGEX, Pattern.UNICODE_CHARACTER_CLASS);
		 Matcher m = p.matcher(line);
		 if(!m.matches()) return false;	
		 m.reset();
		 System.out.println("-----------------------");
		 System.out.println("MODULE_DEPENDENCY_REGEX");
		 while(m.find()) {
		 System.out.println("---------------------------------------------");
		 if(m.start("dependencyType") > -1) {
			 System.out.println("dependencyType: " + m.group("dependencyType"));
		 }
		 if(m.start("Hklm") > -1) {
			 System.out.println("Hklm: " + m.group("Hklm"));
		 }
		 if(m.start("name") > -1) {
			 System.out.println("name: " + m.group("name"));
		 }
	 }
	 return true;		 
	}
	
	public static void main(String[] args) throws FileNotFoundException {
		ParserRegexVb6 parserRegexVb6 = new ParserRegexVb6("C:\\Users\\josez\\eclipse-workspace_2303_03\\parser-vb\\src\\test\\resources\\vb6Teste.frm");
	}
}
