
import java.io.File;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import org.w3c.dom.Element;

import java.util.ArrayList;
import java.util.List;


public class Ybus {
	
	
	public static void main(String[] args) {
		try{
			
		    // Parsing method
			
			// Prepare EQ file
			File XmlFile = new File("Assignment_EQ_reduced.xml");
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			Document doc = dBuilder.parse(XmlFile);
			doc.getDocumentElement().normalize();
			// Prepare SSH file
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
			




        NodeList TerminalList = doc.getElementsByTagName("cim:Terminal");
		NodeList ConnectivityNodeList = doc.getElementsByTagName("cim:ConnectivityNode");
		NodeList LineList = doc.getElementsByTagName("cim:ACLineSegment");
		NodeList BusList = doc.getElementsByTagName("cim:BusbarSection");
		NodeList ShuntList = doc.getElementsByTagName("cim:LinearShuntCompensator");	
		
		double Sbase = 1000 ; //MVA
		double Zbase, puR, puX, puB, puG;
		
		int busnumber = BusList.getLength();
		String Bus[]= new String[busnumber]; 
		Complex zero = new Complex(0,0);		
		Complex Ybus[][] = new Complex[busnumber][busnumber];
		
		
		
		for (int i = 0; i < BusList.getLength(); i++){
			
			  for (int j = 0; j < BusList.getLength(); j++){
			      Ybus[i][j] = zero;
			  }
			  
			  
		}  // bus loop finish 
		
		
		
		
		for (int i = 0; i < BusList.getLength(); i++){
			
            Element element = (Element) BusList.item(i);
		    
			String rdfID = element.getAttribute("rdf:ID");
			Bus[i] = rdfID;
			// System.out.println(rdfID);
		}  // finish 
		
		
		
		for (int i = 0; i < LineList.getLength(); i++){  // Line loop begin
			
			List <String> tempLine = new ArrayList<String>();
			
            Element element = (Element) LineList.item(i);
	    
		    String rdfID_Line = element.getAttribute("rdf:ID");
		    
		    tempLine.add(rdfID_Line);
		    
		    
		    Double Line_r = Double.parseDouble(element.getElementsByTagName("cim:ACLineSegment.r").item(0).getTextContent());
		    Double Line_x = Double.parseDouble(element.getElementsByTagName("cim:ACLineSegment.x").item(0).getTextContent());
		    Double Line_gch = Double.parseDouble(element.getElementsByTagName("cim:ACLineSegment.gch").item(0).getTextContent());
		    Double Line_bch = Double.parseDouble(element.getElementsByTagName("cim:ACLineSegment.bch").item(0).getTextContent());
		    
		    
		    String Line_basevoltage = getID(element, "cim:ConductingEquipment.BaseVoltage");
		    
		   
		    
		    
		    for (int j = 0; j < TerminalList.getLength(); j++) {
		    	
		    	
		    	Element element1 = (Element) TerminalList.item(j);
				String TerminalCondEqID = getID(element1, "cim:Terminal.ConductingEquipment");
				String TerminalConNodeID = getID(element1, "cim:Terminal.ConnectivityNode");
				if (rdfID_Line.equals(TerminalCondEqID)) {
					tempLine.add(TerminalConNodeID); // line's connectivity node was founded and added to the list
					// System.out.println("Line_rdfID: " + rdfID_Line +"\n" + "TerminalConNodeID: "  + TerminalConNodeID +"\n" );
				}
			}  // Terminal loop finish first line was matched with its corresponding connectivity nodes
		    
		    
		   boolean check3=true;
		   boolean check4=true;
		   boolean state1 = false;
			boolean state2 = false;
		   
		   
			// the condition for adding the line to YBUS matrix is checking breaker status
			
				for (int k = 0; k < BreakerList.getLength(); k++) {
					List <String> tempBreaker = new ArrayList<String>();
				    Element elementBreaker = (Element) BreakerList.item(k);
				    Element elementSSH = (Element) BreakerListSSH.item(k);
					
					boolean state_Breaker = Boolean.valueOf(elementSSH.getElementsByTagName("cim:Switch.open").item(0).getTextContent());
				    
					String rdfID_Breaker = elementBreaker.getAttribute("rdf:ID");
					tempBreaker.add(rdfID_Breaker);
					
					 for (int j = 0; j < TerminalList.getLength(); j++) {
						    Element element1 = (Element) TerminalList.item(j);				
						    String TerminalCondEqID = getID(element1, "cim:Terminal.ConductingEquipment");
							String TerminalConNodeID = getID(element1, "cim:Terminal.ConnectivityNode");
							
							
							if (rdfID_Breaker.equals(TerminalCondEqID)) {
								tempBreaker.add(TerminalConNodeID); // breaker's connectivity node was founded and added to the list
								// System.out.println("BreakerrdfID: " + rdfID_Breaker +"\n" + "TerminalConNodeID: " 
						                // + TerminalConNodeID +"\n" );
							    }
						    }  // first breaker was matched with its corresponding connectivity nodes and added to temporary list
					// System.out.println(tempBreaker +"\n");
					
		    // Skipping the breaker and finding the connectivity node at the other side of the breaker
			// if the line's CNode match with the breaker's FIRST CNode, we set the breaker's SECOND CNode to the line
					 
				
				
				if (check3 == true){
				if(tempLine.get(1).equals(tempBreaker.get(1))){
					check3 = false;
					tempLine.set(1,tempBreaker.get(2));
					state1 = state_Breaker;
					
					
					
					
					
				}else if(tempLine.get(1).equals(tempBreaker.get(2))){
					check3=false;
					tempLine.set(1,tempBreaker.get(1));
					state1 = state_Breaker;
				}
				}
				
				if (check4 == true){	
				if(tempLine.get(2).equals(tempBreaker.get(1))){
					check4 = false;
					tempLine.set(2,tempBreaker.get(2));
					state2 = state_Breaker;
				}else if(tempLine.get(2).equals(tempBreaker.get(2))){
					check4=false;
					tempLine.set(2,tempBreaker.get(1));	
					state2 = state_Breaker;
				}
				}
		    } // Breakers were skipped
				
				
				if ( state1 == false && state2 == false){
				for (int m = 0; m < BusList.getLength(); m++){
					List <String> tempBus = new ArrayList<String>();
                    Element elementbus = (Element) BusList.item(m);
				    
					String rdfID_Bus = elementbus.getAttribute("rdf:ID");
					tempBus.add(rdfID_Bus);
					
					for (int j = 0; j < TerminalList.getLength(); j++) {
					    Element element1 = (Element) TerminalList.item(j);				
					    String TerminalCondEqID = getID(element1, "cim:Terminal.ConductingEquipment");
						String TerminalConNodeID = getID(element1, "cim:Terminal.ConnectivityNode");
						
						
						if (rdfID_Bus.equals(TerminalCondEqID)) {
							tempBus.add(TerminalConNodeID); 
							
						    }
					    }  // first breaker was matched with its corresponding connectivity nodes and added to temporary list
					
				///// if the CNodes of the Line and the Bus match with each other, add that bus to the line list
					if(tempLine.get(1).equals(tempBus.get(1))){
						tempLine.set(1,tempBus.get(0));
					}else if(tempLine.get(2).equals(tempBus.get(1))){
						tempLine.set(2,tempBus.get(0));						
					}
				} // the list of first line with its corresponding buses were obtained
				System.out.println("Line,FromBus,ToBus: "+ tempLine + "\n");
				
				// Calculating Zbase and per-unit values
				for (int j = 0; j < BaseList.getLength(); j++) {
					Element elementBase = (Element) BaseList.item(j);				
					String rdfID_BaseVoltage = elementBase.getAttribute("rdf:ID");
					if (Line_basevoltage.equals(rdfID_BaseVoltage)){
						Double Line_basevoltageValue = Double.parseDouble(elementBase.getElementsByTagName("cim:BaseVoltage.nominalVoltage").item(0).getTextContent());
						Zbase = Math.pow(Line_basevoltageValue, 2)/Sbase;
						// System.out.println(Zbase);
						// System.out.println(Line_r);
						puR= Line_r/Zbase;
						puX= Line_x/Zbase;
						puB= (Line_bch*Zbase)/2.0;
						puG= (Line_gch*Zbase)/2.0;
						tempLine.add(Double.toString(puR));
						tempLine.add(Double.toString(puX));
						tempLine.add(Double.toString(puG));
						tempLine.add(Double.toString(puB));
						// System.out.println(tempLine);
					} // adding per-unit values of R, X, G and B to the line list
				}
				
				for (int ii=0; ii <BusList.getLength(); ii++) {
					if(tempLine.get(1).equals(Bus[ii])){
						Complex Z1= new Complex(Double.parseDouble(tempLine.get(3)),Double.parseDouble(tempLine.get(4))); // impedance of the line
						Complex BG1= new Complex(Double.parseDouble(tempLine.get(5)),Double.parseDouble(tempLine.get(6))); // admittance of the line
						Complex one = new Complex(1.0, 0.0);
						Complex y1= one.divides(Z1);
						Complex y11= y1.plus(BG1);
						Ybus[ii][ii]= y11.plus(Ybus[ii][ii]); // adding diagonal elements
						for(int jj=0; jj < BusList.getLength(); jj++){
							if(tempLine.get(2).equals(Bus[jj])){
								Complex Z2= new Complex(Double.parseDouble(tempLine.get(3)),Double.parseDouble(tempLine.get(4)));
								Complex BG2= new Complex(Double.parseDouble(tempLine.get(5)),Double.parseDouble(tempLine.get(6)));
								Complex y2= one.divides(Z2);
								Complex y22= y2.plus(BG2);
								Ybus[jj][jj]= y22.plus(Ybus[jj][jj]); // adding diagonal elements
								Complex neg= new Complex(-1.0, 0.0);
								Complex negy= y2.times(neg);
								Ybus[ii][jj]= negy.plus(Ybus[ii][jj]); // adding non-diagonal elements
								Ybus[jj][ii]= negy.plus(Ybus[jj][ii]); // adding non-diagonal elements
							}
						}
					}
				}
			
				
				
				
				 }  // finish if 
		}	// finish line	
		
		
		// Visualize
		System.out.println("\n"+"YBUS with Line");
		
		for (int iii=0; iii< BusList.getLength();iii++){
			for(int jjj=0; jjj< BusList.getLength(); jjj++){
				System.out.print(Ybus[iii][jjj]+"\t");	
					}
			System.out.println("\n");
		 }
	 // Line finish
		
	
		
		
		
		
		// same process as line
        for (int i = 0; i < PowerTransformerList.getLength(); i++){  // Line loop begin
			
			List <String> tempTransformer = new ArrayList<String>();
			
            Element element = (Element) PowerTransformerList.item(i);
	    
		    String rdfID_Transformer = element.getAttribute("rdf:ID");
		    
		    tempTransformer.add(rdfID_Transformer);
		    
		    
		    
		    
		    for (int j = 0; j < TerminalList.getLength(); j++) {
		    	
		    	
		    	Element element1 = (Element) TerminalList.item(j);
				String TerminalCondEqID = getID(element1, "cim:Terminal.ConductingEquipment");
				String TerminalConNodeID = getID(element1, "cim:Terminal.ConnectivityNode");
				if (rdfID_Transformer.equals(TerminalCondEqID)) {
					tempTransformer.add(TerminalConNodeID); // line's connectivity node was founded and added to the list
					// System.out.println("Line_rdfID: " + rdfID_Line +"\n" + "TerminalConNodeID: "  + TerminalConNodeID +"\n" );
				}
			}  // Terminal loop finish first line was matched with its corresponding connectivity nodes
		    
		    
		   boolean check3=true;
		   boolean check4=true;
		   boolean state1 = false;
			boolean state2 = false;
		   
				for (int k = 0; k < BreakerList.getLength(); k++) {
					List <String> tempBreaker = new ArrayList<String>();
				    Element elementBreaker = (Element) BreakerList.item(k);
				    
				    Element elementSSH = (Element) BreakerListSSH.item(k);
					
					boolean state_Breaker = Boolean.valueOf(elementSSH.getElementsByTagName("cim:Switch.open").item(0).getTextContent());
				    
					String rdfID_Breaker = elementBreaker.getAttribute("rdf:ID");
					tempBreaker.add(rdfID_Breaker);
					
					 for (int j = 0; j < TerminalList.getLength(); j++) {
						    Element element1 = (Element) TerminalList.item(j);				
						    String TerminalCondEqID = getID(element1, "cim:Terminal.ConductingEquipment");
							String TerminalConNodeID = getID(element1, "cim:Terminal.ConnectivityNode");
							
							
							if (rdfID_Breaker.equals(TerminalCondEqID)) {
								tempBreaker.add(TerminalConNodeID); // breaker's connectivity node was founded and added to the list
								
							    }
						    }  // first breaker was matched with its corresponding connectivity nodes and added to temporary list
					// System.out.println(tempBreaker +"\n");
					
		    // Skipping the breaker and finding the connectivity node at the other side of the breaker
			// if the line's CNode match with the breaker's FIRST CNode, we set the breaker's SECOND CNode to the line
				
				if (check3 == true){
				if(tempTransformer.get(1).equals(tempBreaker.get(1))){
					check3 = false;
					tempTransformer.set(1,tempBreaker.get(2));
					state1 = state_Breaker;
					
					
					
					
					
				}else if(tempTransformer.get(1).equals(tempBreaker.get(2))){
					check3=false;
					tempTransformer.set(1,tempBreaker.get(1));
					state1 = state_Breaker;
				}
				}
				
				if (check4 == true){	
				if(tempTransformer.get(2).equals(tempBreaker.get(1))){
					check4 = false;
					tempTransformer.set(2,tempBreaker.get(2));
					state2 = state_Breaker;
				}else if(tempTransformer.get(2).equals(tempBreaker.get(2))){
					check4=false;
					tempTransformer.set(2,tempBreaker.get(1));	
					state2 = state_Breaker;
				}
				}
		    } // Breakers were skipped
				
				
				if ( state1 == false && state2 == false){
				for (int m = 0; m < BusList.getLength(); m++){
					List <String> tempBus = new ArrayList<String>();
                    Element elementbus = (Element) BusList.item(m);
				    
					String rdfID_Bus = elementbus.getAttribute("rdf:ID");
					tempBus.add(rdfID_Bus);
					
					for (int j = 0; j < TerminalList.getLength(); j++) {
					    Element element1 = (Element) TerminalList.item(j);				
					    String TerminalCondEqID = getID(element1, "cim:Terminal.ConductingEquipment");
						String TerminalConNodeID = getID(element1, "cim:Terminal.ConnectivityNode");
						
						
						if (rdfID_Bus.equals(TerminalCondEqID)) {
							tempBus.add(TerminalConNodeID); 
							
						    }
					    }  // first breaker was matched with its corresponding connectivity nodes and added to temporary list
					
				///// if the CNodes of the Line and the Bus match with each other, add that bus to the line list
					if(tempTransformer.get(1).equals(tempBus.get(1))){
						tempTransformer.set(1,tempBus.get(0));
					}else if(tempTransformer.get(2).equals(tempBus.get(1))){
						tempTransformer.set(2,tempBus.get(0));						
					}
				} // the list of first line with its corresponding buses were obtained
				System.out.println("Transformer,FromBus,ToBus: "+ tempTransformer );
				
				
				// Calculating Zbase and per-unit values
				for (int j = 0; j < PowerTransformerEndList.getLength(); j++) {
					
					 Element elemenEnd= (Element) PowerTransformerEndList.item(j);
					
					String rdfID_TransformerEnd = getID(elemenEnd, "cim:PowerTransformerEnd.PowerTransformer");
					
					if (rdfID_TransformerEnd.equals(rdfID_Transformer)){
						Double Transformer_r = Double.parseDouble(elemenEnd.getElementsByTagName("cim:PowerTransformerEnd.r").item(0).getTextContent());
						if (Transformer_r != 0){
							Transformer_r = Double.parseDouble(elemenEnd.getElementsByTagName("cim:PowerTransformerEnd.r").item(0).getTextContent());
							Double Transformer_x = Double.parseDouble(elemenEnd.getElementsByTagName("cim:PowerTransformerEnd.x").item(0).getTextContent());
							Double Transformer_g = Double.parseDouble(elemenEnd.getElementsByTagName("cim:PowerTransformerEnd.g").item(0).getTextContent());
							Double Transformer_b = Double.parseDouble(elemenEnd.getElementsByTagName("cim:PowerTransformerEnd.b").item(0).getTextContent());
							
					       String rdfID_BaseVoltageEnd = getID(elemenEnd, "cim:TransformerEnd.BaseVoltage");
					
				    for (int n = 0; n < BaseList.getLength(); n++) {
					Element elementBase = (Element) BaseList.item(n);				
					String rdfID_BaseVoltage = elementBase.getAttribute("rdf:ID");
					if (rdfID_BaseVoltage.equals(rdfID_BaseVoltageEnd)){
						Double Transformer_basevoltageValue = Double.parseDouble(elementBase.getElementsByTagName("cim:BaseVoltage.nominalVoltage").item(0).getTextContent());
						Zbase = Math.pow(Transformer_basevoltageValue, 2)/Sbase;
						
						puR= Transformer_r/Zbase;
						puX= Transformer_x/Zbase;
						puB= Transformer_b*Zbase;
						puG= Transformer_g*Zbase;
						tempTransformer.add(Double.toString(puR));
						tempTransformer.add(Double.toString(puX));
						tempTransformer.add(Double.toString(puG));
						tempTransformer.add(Double.toString(puB));
						System.out.println(tempTransformer + "\n");
					} // adding per-unit values of R, X, G and B to the line list
				}
				}	
				}
				}
				
				
				
				for (int ii=0; ii <BusList.getLength(); ii++) {
					if(tempTransformer.get(1).equals(Bus[ii])){
						Complex Z1= new Complex(Double.parseDouble(tempTransformer.get(3)),Double.parseDouble(tempTransformer.get(4))); // impedance of the line
						Complex BG1= new Complex(Double.parseDouble(tempTransformer.get(5)),Double.parseDouble(tempTransformer.get(6))); // admittance of the line
						Complex one = new Complex(1.0, 0.0);
						Complex y1= one.divides(Z1);
						Complex y11= y1.plus(BG1);
						Ybus[ii][ii]= y11.plus(Ybus[ii][ii]); // adding diagonal elements
						for(int jj=0; jj < BusList.getLength(); jj++){
							if(tempTransformer.get(2).equals(Bus[jj])){
								Complex Z2= new Complex(Double.parseDouble(tempTransformer.get(3)),Double.parseDouble(tempTransformer.get(4)));
								Complex BG2= new Complex(Double.parseDouble(tempTransformer.get(5)),Double.parseDouble(tempTransformer.get(6)));
								Complex y2= one.divides(Z2);
								Complex y22= y2.plus(BG2);
								Ybus[jj][jj]= y22.plus(Ybus[jj][jj]); // adding diagonal elements
								Complex neg= new Complex(-1.0, 0.0);
								Complex negy= y2.times(neg);
								Ybus[ii][jj]= negy.plus(Ybus[ii][jj]); // adding non-diagonal elements
								Ybus[jj][ii]= negy.plus(Ybus[jj][ii]); // adding non-diagonal elements
							}
						}
					}
				}
			
				
				
				
				 }  // finish if 
				// Visualize
				
		}	// finish Transformer	
		
		
		/* // Visualize
		System.out.println("\n"+"YBUS with Line and Transformer");
		
		for (int iii=0; iii< BusList.getLength();iii++){
			for(int jjj=0; jjj< BusList.getLength(); jjj++){
				System.out.print(Ybus[iii][jjj]+"\t");	
					}
			System.out.println("\n");
		 }
	 // Transformer Visualize finish	*/
	
		
		
		
     // Begin capacitor
		for (int ii=0; ii <ShuntList.getLength(); ii++) {
			Element elementShunt = (Element) ShuntList.item(ii);
			List <String> tempShunt = new ArrayList<String>();
			Double Shunt_b = Double.parseDouble(elementShunt.getElementsByTagName("cim:LinearShuntCompensator.bPerSection").item(0).getTextContent());
			Double Shunt_g = Double.parseDouble(elementShunt.getElementsByTagName("cim:LinearShuntCompensator.gPerSection").item(0).getTextContent());
			
			
			Double shunt_basevoltageValue = Double.parseDouble(elementShunt.getElementsByTagName("cim:ShuntCompensator.nomU").item(0).getTextContent());
			String rdfID_Shunt = elementShunt.getAttribute("rdf:ID");			
			tempShunt.add(rdfID_Shunt);
			String Shunt_EqID = getID(elementShunt,"cim:Equipment.EquipmentContainer");
			
			for (int n=0; n < BusList.getLength(); n++) {
				Element elementBus = (Element) BusList.item(n);
				String rdfID_Bus = elementBus.getAttribute("rdf:ID");
				String Bus_EqID = getID(elementBus,"cim:Equipment.EquipmentContainer");
				if(Shunt_EqID.equals(Bus_EqID)){
					tempShunt.add(rdfID_Bus);
				}
			}
			System.out.println( "\n"+ "Shunt, Bus: "+ tempShunt);
			// Per-unit values
			
			Zbase = Math.pow(shunt_basevoltageValue, 2)/Sbase;
			puB= Shunt_b*Zbase; // B per section
			puG= Shunt_g*Zbase; // G per section
			puB= puB*4; // shunt with 4 sections
			puG= puG*4; // shunt with 4 sections
			tempShunt.add(Double.toString(puG));
			tempShunt.add(Double.toString(puB));
			
	///////////////////////////////////////////////////////
	// Adding to YBUS matrix ******************************
	for (int i=0; i < BusList.getLength(); i++) {
		if(tempShunt.get(1).equals(Bus[i])){
			Complex Y1= new Complex(Double.parseDouble(tempShunt.get(2)),Double.parseDouble(tempShunt.get(3)));
			Ybus[i][i]=Y1.plus(Ybus[i][i]);
			
		}
	}		
}
		// Visualize
				System.out.println("\n"+"YBUS with Line, Transformer and Shunt Capacitor");
				
				for (int iii=0; iii< BusList.getLength();iii++){
					for(int jjj=0; jjj< BusList.getLength(); jjj++){
						System.out.print(Ybus[iii][jjj]+"\t");	
							}
					System.out.println("\n");
				 }
			 // Y matrix visualize finish
				
		
		
		
		
	} // try finish
	catch(Exception e){
		 //Handle errors for Class.forName
		 e.printStackTrace();}
		 
		 
	 System.out.println("Goodbye!");
	 
    } // main finish
		 public static String getID (Element element,String id){
				Node node = element.getElementsByTagName(id).item(0);
				Element elementid = (Element) node;
				String rdf_id = elementid.getAttribute("rdf:resource");
				String rdf_ID = rdf_id.replaceAll("[#]+", "");
				return rdf_ID;
				
			}
		
		 }




