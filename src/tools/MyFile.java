package tools;

import java.io.File;

/**
 * Extended file class, able to give back just the file name without extension
 * @author Daniel Sathees Elmo
 *
 */

public class MyFile extends File{

	/**
	 * serialversionUID
	 */
	private static final long serialVersionUID = 1L;

	
	/**
	 * constructor needs path of file
	 * @param pathname 
	 */
	public MyFile(String pathname) {
		//call super constructor
		super(pathname);
	}
	

	/**
	 * method to extract filename without its extension
	 * @return filename as string without extension
	 */
	public String getNameWithoutExt(){
		//extract file name
		String fileName=super.getName();
		//get position of last dot
		int dotPos=fileName.lastIndexOf(".");
		//if there is no dot, cut whole String
		if (dotPos==-1)
			dotPos=fileName.length();
		//return without extension
		return fileName.substring(0, dotPos);
	}

}
