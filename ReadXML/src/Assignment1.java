
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;


import java.io.File;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import org.w3c.dom.Element;

import com.mysql.jdbc.PreparedStatement;

public class Assignment1 {
	// JDBC driver name and database URL
	 static final String JDBC_DRIVER = "com.mysql.jdbc.Driver";
	 static final String DB_URL = "jdbc:mysql://localhost/";  // :3306
	 // Database credentials
	 static final String USER = "root";
	 static final String PASS = "root"; // insert the password to SQL server

	 public static void main(String[] args) {
	 Connection conn = null;
	 Statement stmt = null;
	 java.sql.PreparedStatement preparedStmt = null;
	 
	 try{
		 String sql;
		 // Register JDBC driver
		 Class.forName(JDBC_DRIVER);
		 
		 // Open a connection
		 
		 
		 System.out.println("Connecting to database...");
		 conn = DriverManager.getConnection(DB_URL, USER, PASS);

		// Execute a query to create database
		 System.out.println("Creating database...");
		
		 stmt = conn.createStatement();
		 
		 sql = "DROP DATABASE IF EXISTS PowerGrid";
		 stmt.executeUpdate(sql);
		 System.out.println("Droping database successfully...");
		 
		 sql = "CREATE DATABASE PowerGrid"; // Create database student
		 stmt.executeUpdate(sql);
		 
		 System.out.println("Database created successfully..."); 
		 
		 conn = DriverManager.getConnection(DB_URL + "PowerGrid", USER, PASS);
		 
		 stmt = conn.createStatement();
		 

		 
		 sql = "CREATE TABLE BaseVoltage (rdfID VARCHAR(40) NOT NULL, NominalValue DOUBLE, PRIMARY KEY (rdfID))" ;
		 stmt.executeUpdate(sql);
		 
		 sql = "CREATE TABLE IF NOT EXISTS Substation"
		 		+ "(rdfID VARCHAR(40) NOT NULL, Name VARCHAR(40), Region_rdfID VARCHAR(40),PRIMARY KEY (rdfID))";
		 stmt.executeUpdate(sql);
		 
		 sql = "CREATE TABLE IF NOT EXISTS VoltageLevel"
					+ "(rdfID VARCHAR(40) NOT NULL, Name VARCHAR(40), Substation_rdfID VARCHAR(40),"
					+ "BaseVoltage_rdfID VARCHAR(40), PRIMARY KEY (rdfID),"
					+ "FOREIGN KEY (Substation_rdfID) REFERENCES Substation(rdfID),"
					+ "FOREIGN KEY (BaseVoltage_rdfID) REFERENCES BaseVoltage(rdfID))";
		 stmt.executeUpdate(sql) ;
		 
		 sql = "CREATE TABLE IF NOT EXISTS GeneratingUnit"
					+ "(rdfID VARCHAR(40) NOT NULL, Name VARCHAR(40), MaxP DOUBLE, MinP DOUBLE,"
					+ "EquipmentContainer_rdfID VARCHAR(40), PRIMARY KEY (rdfID),"
					+ "FOREIGN KEY (EquipmentContainer_rdfID) REFERENCES Substation(rdfID))";
		stmt.executeUpdate(sql) ; 
		
		sql = "CREATE TABLE IF NOT EXISTS SynchronousMachine"
				+ "(rdfID VARCHAR(40) NOT NULL, Name VARCHAR(40), RatedS DOUBLE, P DOUBLE, Q DOUBLE,"
				+ "GenUnit_rdfID VARCHAR(40), RegControl_rdfID VARCHAR(40),"
				+ "EquipmentContainer_rdfID VARCHAR(40), BaseVoltage_rdfID VARCHAR(40), PRIMARY KEY (rdfID),"
				+ "FOREIGN KEY (GenUnit_rdfID) REFERENCES GeneratingUnit(rdfID),"
				+ "FOREIGN KEY (EquipmentContainer_rdfID) REFERENCES VoltageLevel(rdfID),"
				+ "FOREIGN KEY (BaseVoltage_rdfID) REFERENCES BaseVoltage(rdfID))";
		stmt.executeUpdate(sql) ; 
		
		
		
		
		
		
		
		sql = "CREATE TABLE IF NOT EXISTS RegulatingControl"
				+ "(rdfID VARCHAR(40) NOT NULL, Name VARCHAR(40), TargetValue DOUBLE,"
				+ "PRIMARY KEY (rdfID))";
		stmt.executeUpdate(sql) ; // execute query
		
					
		sql = "CREATE TABLE IF NOT EXISTS PowerTransformer"
				+ "(rdfID VARCHAR(40) NOT NULL, Name VARCHAR(40),"
				+ "EquipmentContainer_rdfID VARCHAR(40), PRIMARY KEY (rdfID),"
				+ "FOREIGN KEY (EquipmentContainer_rdfID) REFERENCES Substation(rdfID))";
		stmt.executeUpdate(sql) ; // execute query
		
		
		// Create Energy Consumer table with corresponding attributes
		sql = "CREATE TABLE IF NOT EXISTS EnergyConsumer"
				+ "(rdfID VARCHAR(40) NOT NULL, Name VARCHAR(40), P DOUBLE, Q DOUBLE,"
				+ "EquipmentContainer_rdfID VARCHAR(40), BaseVoltage_rdfID VARCHAR(40),PRIMARY KEY (rdfID),"
				+ "FOREIGN KEY (EquipmentContainer_rdfID) REFERENCES VoltageLevel(rdfID),"
				+ "FOREIGN KEY (BaseVoltage_rdfID) REFERENCES BaseVoltage(rdfID))";
		stmt.executeUpdate(sql) ; // execute query
		
		
		 // Create Transformer Winding table with corresponding attributes
		sql = "CREATE TABLE IF NOT EXISTS PowerTransformerEnd"
				+ "(rdfID VARCHAR(40) NOT NULL, Name VARCHAR(40), TransformerR DOUBLE,"
				+ "TransformerX DOUBLE, Transformer_rdfID VARCHAR(40), BaseVoltage_rdfID VARCHAR(40),"
				+ "PRIMARY KEY (rdfID), FOREIGN KEY(Transformer_rdfID) REFERENCES PowerTransformer(rdfID),"
				+ "FOREIGN KEY(BaseVoltage_rdfID) REFERENCES BaseVoltage(rdfID))";
		stmt.executeUpdate(sql) ; // execute query
		
		
		// Create Breaker table with corresponding attributes
		sql = "CREATE TABLE IF NOT EXISTS Breaker"
				+ "(rdfID VARCHAR(40) NOT NULL, Name VARCHAR(40), State BOOLEAN,"
				+ "EquipmentContainer_rdfID VARCHAR(40), BaseVoltage_rdfID VARCHAR(40), PRIMARY KEY (rdfID),"
				+ "FOREIGN KEY (EquipmentContainer_rdfID) REFERENCES VoltageLevel(rdfID), "
				+ "FOREIGN KEY(BaseVoltage_rdfID) REFERENCES BaseVoltage(rdfID))";
		stmt.executeUpdate(sql) ; // execute query
		
		
		// Create Ratio Tap Changer table with corresponding attributes
		sql = "CREATE TABLE IF NOT EXISTS RatioTapChanger"
				+ "(rdfID VARCHAR(40) NOT NULL, Name VARCHAR(40), Step DOUBLE,"
				+ "PRIMARY KEY (rdfID))";					
		stmt.executeUpdate(sql) ; // execute query
		
	   
		 
		System.out.println("Created table in given database successfully..." + "\n"); 
		 
		 
		 // Prepare for parsing
		 
		
		File XmlFile = new File("Assignment_EQ_reduced.xml");
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
		Document doc = dBuilder.parse(XmlFile);
		doc.getDocumentElement().normalize();
		
		File XmlFileSSH = new File("Assignment_SSH_reduced.xml");
		DocumentBuilderFactory dbFactorySSH = DocumentBuilderFactory.newInstance();
		DocumentBuilder dBuilderSSH = dbFactorySSH.newDocumentBuilder();
		Document docSSH = dBuilderSSH.parse(XmlFileSSH); 
		docSSH.getDocumentElement().normalize();
		
		NodeList BaseList = doc.getElementsByTagName("cim:BaseVoltage");
		NodeList SubstationList = doc.getElementsByTagName("cim:Substation");
		NodeList VoltList = doc.getElementsByTagName("cim:VoltageLevel");
		NodeList GeneratingList = doc.getElementsByTagName("cim:GeneratingUnit");
		NodeList SynchronousMachineList = doc.getElementsByTagName("cim:SynchronousMachine");
		NodeList RegulatingControlList = doc.getElementsByTagName("cim:RegulatingControl");
		NodeList PowerTransformerList = doc.getElementsByTagName("cim:PowerTransformer");
		NodeList EnergyConsumerList = doc.getElementsByTagName("cim:EnergyConsumer");
		NodeList PowerTransformerEndList = doc.getElementsByTagName("cim:PowerTransformerEnd");
		NodeList BreakerList = doc.getElementsByTagName("cim:Breaker");
		NodeList RatioTapChangerList = doc.getElementsByTagName("cim:RatioTapChanger");
		
		
		NodeList SynchronousMachineListSSH = docSSH.getElementsByTagName("cim:SynchronousMachine");
		NodeList RegulatingControlListSSH = docSSH.getElementsByTagName("cim:RegulatingControl");
		NodeList EnergyConsumerListSSH = docSSH.getElementsByTagName("cim:EnergyConsumer");
		NodeList BreakerListSSH = docSSH.getElementsByTagName("cim:Breaker");
		NodeList RatioTapChangerListSSH = docSSH.getElementsByTagName("cim:RatioTapChanger");
		
		
		 for (int i = 0; i < BaseList.getLength(); i++) {
			
			Element element = (Element) BaseList.item(i);
			
			String rdfID = element.getAttribute("rdf:ID");
			
			Double NominalValue = Double.parseDouble(element.getElementsByTagName("cim:BaseVoltage.nominalVoltage").item(0).getTextContent());
			
			
			String query = "INSERT INTO BaseVoltage VALUES(?,?)";
			preparedStmt = conn.prepareStatement(query);
			preparedStmt.setString(1,rdfID);
			preparedStmt.setDouble(2,NominalValue);
			preparedStmt.executeUpdate(); // execute PreparedStatement
			System.out.println("Inserted records into the table...");


			
			System.out.println("rdfID: " + rdfID +"\n" + "nominal value " + NominalValue +"\n" );
			
			}
		
		for (int i = 0; i <SubstationList.getLength(); i++) {
			
			Element element = (Element) SubstationList.item(i);
			
			String rdfID = element.getAttribute("rdf:ID");
			
			String name = element.getElementsByTagName("cim:IdentifiedObject.name").item(0).getTextContent();
			
			
			String region_rdfID = getID(element,"cim:Substation.Region");
			
			
			String query = "INSERT INTO Substation VALUES(?,?,?)";
			preparedStmt = conn.prepareStatement(query);
			preparedStmt.setString(1,rdfID);
			preparedStmt.setString(2,name);
			preparedStmt.setString(3,region_rdfID);
			
			preparedStmt.executeUpdate(); // execute PreparedStatement
			System.out.println("Inserted records into the table...");


			
			System.out.println("rdfID: " + rdfID +"\n" + "name: " + name +"\n" + "region_rdf:ID " + region_rdfID + "\n");;
			
			}
		
		for (int i = 0; i < VoltList.getLength(); i++) {
			 
			 
		    Element element = (Element) VoltList.item(i);
		    
			String rdfID = element.getAttribute("rdf:ID");
			
			
			Double name = Double.parseDouble(element.getElementsByTagName("cim:IdentifiedObject.name").item(0).getTextContent());
			
			String substation_rdfID = getID(element, "cim:VoltageLevel.Substation");
			
			String basevoltage_rdfID = getID(element, "cim:VoltageLevel.BaseVoltage");
			
			
			System.out.println("Inserted records into the table...");
			String query = "INSERT INTO VoltageLevel VALUES(?,?,?,?)";
			preparedStmt = conn.prepareStatement(query);
			preparedStmt.setString(1,rdfID);
			preparedStmt.setDouble(2,name);
			preparedStmt.setString(3,substation_rdfID);
			preparedStmt.setString(4,basevoltage_rdfID);
			
			preparedStmt.executeUpdate(); // execute PreparedStatement
			System.out.println("rdfID: " + rdfID +"\n" + "name: " + name +"\n" + "substation_rdf:ID " + substation_rdfID + "\n" + "baseVoltage_rdf:ID " + basevoltage_rdfID + "\n");

			
		} 
		
		 for (int i = 0; i < GeneratingList.getLength(); i++) {
			 
			 
			    Element element = (Element) GeneratingList.item(i);
			    
				String rdfID = element.getAttribute("rdf:ID");
				
				String name = element.getElementsByTagName("cim:IdentifiedObject.name").item(0).getTextContent();
				
				
				
				Double maxP = Double.parseDouble(element.getElementsByTagName("cim:GeneratingUnit.maxOperatingP").item(0).getTextContent());
				
				
				Double minP = Double.parseDouble(element.getElementsByTagName("cim:GeneratingUnit.minOperatingP").item(0).getTextContent());

				
				String equipmentContainer_rdfID = getID(element, "cim:Equipment.EquipmentContainer");
				
				System.out.println("Inserted records into the table...");
				String query = "INSERT INTO GeneratingUnit VALUES(?,?,?,?,?)";
				preparedStmt = conn.prepareStatement(query);
				preparedStmt.setString(1,rdfID);
				preparedStmt.setString(2,name);
				preparedStmt.setDouble(3,maxP);
				preparedStmt.setDouble(4,minP);
				preparedStmt.setString(5,equipmentContainer_rdfID);
				preparedStmt.executeUpdate(); // execute PreparedStatement
				
				System.out.println("rdfID: " + rdfID +"\n" + "name: " + name +"\n" + "maxP: " + maxP + "\n" + "minP: " + minP + "\n" + "equipmentContainer_rdf:ID " + equipmentContainer_rdfID + "\n");
				
			} 
		
		for (int i = 0; i <SynchronousMachineList.getLength(); i++) {
			 
			 
		    Element element = (Element) SynchronousMachineList.item(i);
		    
			String rdfID = element.getAttribute("rdf:ID");
			
			String name = element.getElementsByTagName("cim:IdentifiedObject.name").item(0).getTextContent();
			
			
			
			Double ratedS = Double.parseDouble(element.getElementsByTagName("cim:RotatingMachine.ratedS").item(0).getTextContent());
			
			
			String genUnit_rdfID = getID(element, "cim:RotatingMachine.GeneratingUnit");
			String regControl_rdfID = getID(element, "cim:RegulatingCondEq.RegulatingControl");
			
			
			
			

			
		    String equipmentContainer_rdfID = getID(element, "cim:Equipment.EquipmentContainer");
		    String baseVoltage_rdfID = null;
		    
		    for (int j = 0; j < VoltList.getLength(); j++) {
		    Element element1 = (Element) VoltList.item(j);
			String n = element1.getAttribute("rdf:ID");
			
			
			if (n.equals(equipmentContainer_rdfID)) {
				baseVoltage_rdfID = getID(element1,"cim:VoltageLevel.BaseVoltage");
			}
			
		    }
				
			Element elementSSH = (Element) SynchronousMachineListSSH.item(i);
			Double P = null;
			Double Q = null;
			
			P = Double.parseDouble(elementSSH.getElementsByTagName("cim:RotatingMachine.p").item(0).getTextContent());
			Q = Double.parseDouble(elementSSH.getElementsByTagName("cim:RotatingMachine.q").item(0).getTextContent());

		
			
			System.out.println("Inserted records into the table...");
			String query = "INSERT INTO SynchronousMachine VALUES(?,?,?,?,?,?,?,?,?)";
			preparedStmt = conn.prepareStatement(query);
			preparedStmt.setString(1,rdfID);
			preparedStmt.setString(2,name);
			preparedStmt.setDouble(3,ratedS);
			preparedStmt.setDouble(4,P);
			preparedStmt.setDouble(5,Q);
			preparedStmt.setString(7,regControl_rdfID);
			preparedStmt.setString(6,genUnit_rdfID);
			preparedStmt.setString(8,equipmentContainer_rdfID);
			preparedStmt.setString(9,baseVoltage_rdfID);
			preparedStmt.executeUpdate(); // execute PreparedStatement

			
			System.out.println("rdfID: " + rdfID +"\n" + "name: " 
			                 + name +"\n" + "ratedS " + ratedS + "\n" + "P " + P + "\n"
			                 +"Q " + Q + "\n" + "genUnit_rdfID " + genUnit_rdfID + "\n"
			                 + "regControl_rdfID " + regControl_rdfID + "\n"
					         + "equipmentContainer_rdf:ID " + equipmentContainer_rdfID + "\n"
					         +"baseVoltage_rdfID "+ baseVoltage_rdfID + "\n");
			
		} 
		for (int i = 0; i < RegulatingControlList.getLength(); i++) {
			 
			
		    Element element = (Element) RegulatingControlList.item(i);
		    
			String rdfID = element.getAttribute("rdf:ID");
			
			String name = element.getElementsByTagName("cim:IdentifiedObject.name").item(0).getTextContent();
			
			Element elementSSH = (Element) RegulatingControlListSSH.item(i);
			
			
			
			Double targetValue = Double.parseDouble(elementSSH.getElementsByTagName("cim:RegulatingControl.targetValue").item(0).getTextContent());

		
			
			System.out.println("Inserted records into the table...");
			String query = "INSERT INTO RegulatingControl VALUES(?,?,?)";
			preparedStmt = conn.prepareStatement(query);
			preparedStmt.setString(1,rdfID);
			preparedStmt.setString(2,name);
			preparedStmt.setDouble(3,targetValue);
			preparedStmt.executeUpdate(); // execute PreparedStatement

			
			System.out.println("rdfID: " + rdfID +"\n" + "name: " 
			                 + name +"\n" + "targetValue " + targetValue + "\n");
			
		} 
		
for (int i = 0; i < PowerTransformerList.getLength(); i++) {
			
			Element element = (Element) PowerTransformerList.item(i);
			
			String rdfID = element.getAttribute("rdf:ID");
			
			String name = element.getElementsByTagName("cim:IdentifiedObject.name").item(0).getTextContent();
			
			
			String equipmentContainer_rdfID = getID(element,"cim:Equipment.EquipmentContainer");
			
			
			String query = "INSERT INTO PowerTransformer VALUES(?,?,?)";
			preparedStmt = conn.prepareStatement(query);
			preparedStmt.setString(1,rdfID);
			preparedStmt.setString(2,name);
			preparedStmt.setString(3,equipmentContainer_rdfID);
			
			preparedStmt.executeUpdate(); // execute PreparedStatement
			System.out.println("Inserted records into the table...");


			
			System.out.println("rdfID: " + rdfID +"\n" + "name: " + name +"\n" + "equipmentContainer_rdfID: " + equipmentContainer_rdfID + "\n");;
			
			} 
		
		
		for (int i = 0; i < EnergyConsumerList.getLength(); i++) {
			 
			 
		    Element element = (Element) EnergyConsumerList.item(i);
		    
			String rdfID = element.getAttribute("rdf:ID");
			
			String name = element.getElementsByTagName("cim:IdentifiedObject.name").item(0).getTextContent();
			
		    String equipmentContainer_rdfID = getID(element, "cim:Equipment.EquipmentContainer");
		    String baseVoltage_rdfID = null;
		    
		    for (int j = 0; j < VoltList.getLength(); j++) {
		    Element element1 = (Element) VoltList.item(j);
			String n = element1.getAttribute("rdf:ID");
			
			
			if (n.equals(equipmentContainer_rdfID)) {
				baseVoltage_rdfID = getID(element1,"cim:VoltageLevel.BaseVoltage");
			}
			
		    }
				
			Element elementSSH = (Element) EnergyConsumerListSSH.item(i);
			Double P = null;
			Double Q = null;
			
			P = Double.parseDouble(elementSSH.getElementsByTagName("cim:EnergyConsumer.p").item(0).getTextContent());
			Q = Double.parseDouble(elementSSH.getElementsByTagName("cim:EnergyConsumer.q").item(0).getTextContent());

		
			
			System.out.println("Inserted records into the table...");
			String query = "INSERT INTO EnergyConsumer VALUES(?,?,?,?,?,?)";
			preparedStmt = conn.prepareStatement(query);
			preparedStmt.setString(1,rdfID);
			preparedStmt.setString(2,name);
			
			preparedStmt.setDouble(3,P);
			preparedStmt.setDouble(4,Q);
			
			preparedStmt.setString(5,equipmentContainer_rdfID);
			preparedStmt.setString(6,baseVoltage_rdfID);
			preparedStmt.executeUpdate(); // execute PreparedStatement

			
			System.out.println("rdfID: " + rdfID +"\n" + "name: " 
			                 + name +"\n" + "P " + P + "\n"
			                 +"Q " + Q + "\n" + "genUnit_rdfID " 
					         + "equipmentContainer_rdf:ID " + equipmentContainer_rdfID + "\n"
					         +"baseVoltage_rdfID "+ baseVoltage_rdfID + "\n");
			
		} 
		
		for (int i = 0; i < PowerTransformerEndList.getLength(); i++) {
			 
			 
		    Element element = (Element) PowerTransformerEndList.item(i);
		    
			String rdfID = element.getAttribute("rdf:ID");
			
			String name = element.getElementsByTagName("cim:IdentifiedObject.name").item(0).getTextContent();
			
		    
		
		    Double transformer_r = Double.parseDouble(element.getElementsByTagName("cim:PowerTransformerEnd.r").item(0).getTextContent());
			Double transformer_x = Double.parseDouble(element.getElementsByTagName("cim:PowerTransformerEnd.x").item(0).getTextContent());
		
			String transformer_rdfID = getID(element, "cim:PowerTransformerEnd.PowerTransformer");
			String baseVoltage_rdfID  = getID(element, "cim:TransformerEnd.BaseVoltage");
			
			System.out.println("Inserted records into the table...");
			String query = "INSERT INTO PowerTransformerEnd VALUES(?,?,?,?,?,?)";
			preparedStmt = conn.prepareStatement(query);
			preparedStmt.setString(1,rdfID);
			preparedStmt.setString(2,name);
			
			preparedStmt.setDouble(3,transformer_r);
			preparedStmt.setDouble(4,transformer_x);
			
			preparedStmt.setString(5,transformer_rdfID);
			preparedStmt.setString(6,baseVoltage_rdfID);
			preparedStmt.executeUpdate(); // execute PreparedStatement

			
			System.out.println("rdfID: " + rdfID +"\n" + "name: " 
			                 + name +"\n" + "transformer_r " + transformer_r + "\n"
			                 +"transformer_x " + transformer_x + "\n" + "genUnit_rdfID " 
					         + "equipmentContainer_rdf:ID " + transformer_rdfID + "\n"
					         +"baseVoltage_rdfID "+ baseVoltage_rdfID + "\n");
			
		} 
		
		for (int i = 0; i < BreakerList.getLength(); i++) {
			 
			 
		    Element element = (Element) BreakerList.item(i);
		    
			String rdfID = element.getAttribute("rdf:ID");
			
			String name = element.getElementsByTagName("cim:IdentifiedObject.name").item(0).getTextContent();
			
		    String equipmentContainer_rdfID = getID(element, "cim:Equipment.EquipmentContainer");
		    
		    String baseVoltage_rdfID = null;
		    
		    for (int j = 0; j < VoltList.getLength(); j++) {
		    Element element1 = (Element) VoltList.item(j);
			String n = element1.getAttribute("rdf:ID");
			
			
			if (n.equals(equipmentContainer_rdfID)) {
				baseVoltage_rdfID = getID(element1,"cim:VoltageLevel.BaseVoltage");
			}
			
		    }
				
			Element elementSSH = (Element) BreakerListSSH.item(i);
			
			boolean state = Boolean.valueOf(elementSSH.getElementsByTagName("cim:Switch.open").item(0).getTextContent());
			
			System.out.println("Inserted records into the table...");
			String query = "INSERT INTO Breaker VALUES(?,?,?,?,?)";
			preparedStmt = conn.prepareStatement(query);
			preparedStmt.setString(1,rdfID);
			preparedStmt.setString(2,name);
			
			preparedStmt.setBoolean(3,state);
			
			preparedStmt.setString(4,equipmentContainer_rdfID);
			preparedStmt.setString(5,baseVoltage_rdfID);
			preparedStmt.executeUpdate(); // execute PreparedStatement

			
			System.out.println("rdfID: " + rdfID +"\n" + "name: " 
			                 + name + "\n" + "state " + state + "\n"
					         + "equipmentContainer_rdf:ID " + equipmentContainer_rdfID + "\n"
					         +"baseVoltage_rdfID "+ baseVoltage_rdfID + "\n");
			
		} 
		
		for (int i = 0; i < RatioTapChangerList.getLength(); i++) {
			 
			 
		    Element element = (Element) RatioTapChangerList.item(i);
		    
			String rdfID = element.getAttribute("rdf:ID");
			
			String name = element.getElementsByTagName("cim:IdentifiedObject.name").item(0).getTextContent();
			
		    
			Element elementSSH = (Element) RatioTapChangerListSSH.item(i);
			
			
			String step = elementSSH.getElementsByTagName("cim:TapChanger.step").item(0).getTextContent();
			
			
			System.out.println("Inserted records into the table...");
			String query = "INSERT INTO RatioTapChanger VALUES(?,?,?)";
			preparedStmt = conn.prepareStatement(query);
			preparedStmt.setString(1,rdfID);
			preparedStmt.setString(2,name);
			
			preparedStmt.setString(3,step);
			preparedStmt.executeUpdate(); // execute PreparedStatement
			

			
			System.out.println("rdfID: " + rdfID +"\n" + "name: " 
			                 + name +"\n" + "step " + step + "\n" );
			
		} 
	
		 
		 
         conn.close(); 
		 
		 

		
		 
		 
	 }catch(SQLException se){
			 //Handle errors for JDBC
			 se.printStackTrace();
			 }catch(Exception e){
			 //Handle errors for Class.forName
			 e.printStackTrace();}
			 System.out.println("Goodbye!");
	 }
	 
	 public static String getID (Element element,String id){
			Node node = element.getElementsByTagName(id).item(0);
			Element elementid = (Element) node;
			String rdf_id = elementid.getAttribute("rdf:resource");
			String rdf_ID = rdf_id.replaceAll("[#]+", "");
			return rdf_ID;
			
		}
	 
	 

	 
	 
	 }
	 
