package models;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.hibernate.id.IdentityGenerator.GetGeneratedKeysDelegate;

import play.Play;

public class BPEL {

	public String name;
	
	public String state;
	
	public String description;
	
	public File folder;
	
	public static final List<BPEL> getAll() {
		File store = getBPELStorage();
		List<BPEL> result = new ArrayList<BPEL>();
		if (store != null && store.isDirectory()) {
			File[] folders = store.listFiles();
			for (File file : folders) {
				if (file.isDirectory()) {
					result.add(get(file.getName()));
				}
			}			
		}
		return result;
	}
	
	public static final BPEL get(String name) {
		if (name == null) {
			return null;
		}
		
		File bpelStore = getBPELStorage(name);
		if (bpelStore == null || !bpelStore.isDirectory()) {
			return null;
		}
		
		BPEL bpel = new BPEL();
		bpel.name = name;
		bpel.state = "";
		bpel.description = "";
		bpel.folder = bpelStore;
		return bpel;
	}
	
	private static final File getBPELStorage() {
		return Play.getFile("store/bpel");
	}
	
	private static final File getBPELStorage(String name) {
		File store = getBPELStorage();
		if (store == null) {
			return null;
		}
		return new File(store, name);
	}
	
}
