package wplant;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import javax.xml.rpc.ServiceException;

import org.pathvisio.core.model.ConverterException;
import org.pathvisio.core.model.Pathway;
import org.pathvisio.wikipathways.webservice.WSCurationTag;
import org.pathvisio.wikipathways.webservice.WSPathwayInfo;
import org.wikipathways.client.WikiPathwaysClient;

public class PlantUploader {

	/**
	 * arguments: 
	 * 1 = pathway directory or file 
	 * 2 = username 
	 * 3 = password 
	 * 4 = update comment 
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		System.out.println(args.length);
		if (args.length == 4) {
			String pathwayDir = args[0];
			String username = args[1];
			String password = args[2];
			String comment = args[3];
			File pathwayfile = new File(pathwayDir);

			PlantUploader uploader;

			try {
				uploader = new PlantUploader(pathwayfile,
						username, password, comment);
				try {
					wpIDList.clear();
//					uploader.createPathwayLists();
//					System.out.println(zeaPs);
//					System.out.println("Duplicates in zea");
//					uploader.findDuplicates(zeaPs);
//					System.out.println("done");
//					System.out.println("Duplicates in arabi");
//					uploader.findDuplicates(arabiPs);
//					System.out.println("done");
//					System.out.println("Duplicates in oryza");
//					uploader.findDuplicates(oryzaPs);
//					System.out.println("done");
					if (uploader.checkPathwayDir()) {
						System.out
								.println("pathway directory and organism are valid.\nretrieve pathways...");
						uploader.readGpmlFiles();
						uploader.replacePathways();
								}
				} catch (Exception e) {
					System.out
							.println("could not retrieve pathways from wikipathways\n"
									+ e.getMessage());
				}
			} catch (Exception e1) {
				e1.printStackTrace();
				System.out.println("cannot connect to WP\t" + e1.getMessage());
			}

		} else {
			System.out
					.println("please provide organism name, pathway directory, username, password, update comment.");
		}
	}
	public void readGpmlFiles() {
		for (File file : pathwayDir.listFiles()) {
			readSingleGpmlFile(file);
		}
	}

	public Pathway readSingleGpmlFile(File pathwayfile) {
		Pathway pathway = new Pathway();
		try {

			pathway.readFromXml(pathwayfile, true);
			pathwaysToUpload.put(pathway.getMappInfo().getMapInfoName(),
					pathway);
		} catch (ConverterException e) {
			System.out.println("could not parse pathway from "
					+ pathwayfile.getAbsolutePath());
		}
		return pathway;

	}	
	public void replacePathways() {
		try {
			client.login(username, password);
		} catch (RemoteException e) {
			System.out
					.println("not able to use this user. check password and permission status.");
		}

		for (String name : pathwaysToUpload.keySet()) {
			Pathway p = pathwaysToUpload.get(name);
			
				try {
					WSPathwayInfo info = client.createPathway(p);
					newPathways.add(info.getId() + ": " + info.getName());
					System.out.println("NEW PATHWAY\t" + name + "\t"
							+ info.getId());
				} catch (Exception e) {
					System.out.println("could not upload new pathway " + name);
				}
			}
		

		
		System.out.println("Wikipathways ID list" + wpIDList);
		System.out.println("New pathways\t" + newPathways.size() + "\t"
				+ newPathways);
		
	}


	private static String testwikipathwaysURL = "http://test2.wikipathways.org/wpi/webservice/webservice.php";
	private static String wikipathwaysURL = "http://wikipathways.org/wpi/webservice/webservice.php";
	private final Map<String, WSPathwayInfo> zeaPathways;
	private final Map<String, WSPathwayInfo> arabiPathways;
	private final Map<String, WSPathwayInfo> oryzaPathways;
	private static Map<String, String> wpIDList;
	private final WikiPathwaysClient client;
	private final File pathwayDir;
	private final Map<String, Pathway> pathwaysToUpload;
	
	private final String username;
	private final String password;
	private final String comment;

	private final List<String> newPathways;
	
	private static List<String> zeaPs;
	private static List<String> arabiPs;
	private static List<String> oryzaPs;

	public PlantUploader(File pathwayDir,
			String username, String password, String comment)
			throws MalformedURLException, ServiceException {
		zeaPathways = new HashMap<String, WSPathwayInfo>();
		arabiPathways = new HashMap<String, WSPathwayInfo>();
		oryzaPathways = new HashMap<String, WSPathwayInfo>();
		wpIDList = new HashMap<String, String>();
		client = new WikiPathwaysClient(new URL(wikipathwaysURL));
		this.pathwayDir = pathwayDir;
		pathwaysToUpload = new HashMap<String, Pathway>();
		newPathways = new ArrayList<String>();
		zeaPs = new ArrayList<String>();
		arabiPs = new ArrayList<String>();
		oryzaPs = new ArrayList<String>();
		this.username = username;
		this.password = password;
		this.comment = comment;
		}

	public void createPathwayLists() {
	for (File file : pathwayDir.listFiles()) {
			Pathway pathway = new Pathway();
			try {
				pathway.readFromXml(file, true);
				String organism = pathway.getMappInfo().getOrganism();
				if(organism.equalsIgnoreCase("Arabidopsis thaliana")){
					arabiPs.add(pathway.getMappInfo().getMapInfoName());
					}
				else {
					if(organism.equalsIgnoreCase("Oryza sativa")){
						oryzaPs.add(pathway.getMappInfo().getMapInfoName());
				}else{
					zeaPs.add(pathway.getMappInfo().getMapInfoName());
				}
				}
				} catch (ConverterException e) {
				System.out.println("could not parse pathway from "
						+ file.getAbsolutePath());
			}
			catch (SecurityException e) {  
		        e.printStackTrace();  
		    } 
					}
		System.out.println(arabiPs);
	}
	
	private void findDuplicates(Collection<String> list) {

	    Set<String> duplicates = new LinkedHashSet<String>();
	    Set<String> uniques = new HashSet<String>();

	    for(String t : list) {
	        if(!uniques.add(t)) {
	            duplicates.add(t);
	        }
	    }

	    System.out.println(duplicates);;
	}
	
	public boolean checkPathwayDir() {
		if (!pathwayDir.exists()) {
			System.out.println("directory does not exist\t"
					+ pathwayDir.getAbsolutePath());
			return false;
		}
		if (pathwayDir.isDirectory()) {
			for (File f : pathwayDir.listFiles()) {
				if (f.getName().endsWith(".gpml")) {
					return true;
				}
			}
		}
		System.out
				.println("Location is no directory or does not contain gpml files.\t"
						+ pathwayDir.getAbsolutePath());
		return false;
	}

	
}
