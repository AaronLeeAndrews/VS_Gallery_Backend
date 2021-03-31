package Controllers;

import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

import javax.activation.FileTypeMap;
import javax.imageio.ImageIO;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import org.json.JSONArray;
import org.json.JSONObject;
import com.box.sdk.BoxAPIConnection;
import com.box.sdk.BoxFile;
import com.box.sdk.BoxSharedLink;

@RestController
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class Backend {

	String acc_tok_standalone = "eD7D7xBxVzsNmJmWci3kXPOW7KUr8nPD";
	String acc_tok = "access_token=eD7D7xBxVzsNmJmWci3kXPOW7KUr8nPD";
	
	@RequestMapping(value="/")
	public String homePage() {
		return "Welcome home";
	}
	
	@RequestMapping(value="/pic/{imgFileId}")
    public ResponseEntity<byte[]> getImageById(@PathVariable("imgFileId") String imgFileId) throws IOException{
		System.out.println("/pic/id accessed with id "+ imgFileId +" at url https://api.box.com/2.0/files/"+imgFileId+"?"+acc_tok);

		URL url = new URL("https://api.box.com/2.0/files/"+imgFileId+"?"+acc_tok);
    	HttpURLConnection con = (HttpURLConnection) url.openConnection();
    	con.setRequestMethod("GET");
    	con.setConnectTimeout(15 * 1000);
    	BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
		System.out.println("Ready to start getting lines from the file info");	
    	String inputLine = "";
		JSONObject jo = new JSONObject();
		while ((inputLine = in.readLine()) != null) {
			jo = new JSONObject(inputLine);
		}
		System.out.println("jo: "+jo.toString());
		
		String imgName = jo.getString("name");
    	url = new URL("https://api.box.com/2.0/files/"+imgFileId+"/content?"+acc_tok);
		System.out.println("url content fetch as "+ url.toString());
    	BufferedImage dImg = ImageIO.read(url);
		System.out.println("img name is "+ imgName);
    	File img;
    	if(imgName.contains(".jpg")) {
    		img = new File("downloaded.jpg");
    		ImageIO.write(dImg, "jpg", img);
    	}
    	else {
    		img = new File("downloaded.png");
    		ImageIO.write(dImg, "png", img);
    	}
        return ResponseEntity.ok().contentType(MediaType.valueOf(FileTypeMap.getDefaultFileTypeMap().getContentType(img))).body(Files.readAllBytes(img.toPath()));
    }
	
	@RequestMapping(value="/pic/{folderId}/{imgName}")
    public ResponseEntity<byte[]> getImageByGameFolderId(@PathVariable("folderId") String folderId,
    		@PathVariable("imgName") String imgName) throws IOException{
		System.out.println("/pic/{folderId}/{imgName} accessed with folderId "+ folderId +" and imgName " + imgName);

		String fileId = GetFileIdInFolder(folderId, imgName);
		if(fileId != "") {
	    	URL imgUrl = new URL("https://api.box.com/2.0/files/"+fileId+"/content?"+acc_tok);
	    	/*BufferedImage dImg = ImageIO.read(imgUrl);
			String imgWithExt = GetImgNameWithExtInFolder(folderId, imgName); 
			System.out.println("img name is "+ imgWithExt);
	    	File img;
	    	if(imgWithExt.contains(".jpg")) {
	    		img = new File("downloaded.jpg");
	    		ImageIO.write(dImg, "jpg", img);
	    	}
	    	else {
	    		img = new File("downloaded.png");
	    		ImageIO.write(dImg, "png", img);
	    	}
	
	        return ResponseEntity.ok().contentType(MediaType.valueOf(FileTypeMap.
	        		getDefaultFileTypeMap().getContentType(img))).body(Files.readAllBytes(img.toPath()));*/
	    	ByteArrayOutputStream baos = new ByteArrayOutputStream();
	    	InputStream inputStrm = imgUrl.openStream();
	    	byte[] imgBuffer = new byte[4096];
	    	int bytesRead = -1;
	    	while((bytesRead = inputStrm.read(imgBuffer)) > 0) {
	    		baos.write(imgBuffer);
	    	}
			return new ResponseEntity<byte[]>(baos.toByteArray(), HttpStatus.OK);
		}
		return new ResponseEntity<byte[]>(new byte[0], HttpStatus.NOT_FOUND);
    }
    
	@RequestMapping(value="/text/{id}")
    public ResponseEntity<String> getTextById(@PathVariable("id") String id) throws IOException{
		System.out.println("/text/id accessed with id "+ id +" at url https://api.box.com/2.0/files/"+id+"?"+acc_tok);
    	URL url = new URL("https://api.box.com/2.0/files/"+id+"/content?"+acc_tok);

    	HttpURLConnection con = (HttpURLConnection) url.openConnection();
    	con.setRequestMethod("GET");
    	con.setConnectTimeout(15 * 1000);
    	
    	BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
    	String inputLine = "";
    	String inputLine1 = "";
    	StringBuffer content = new StringBuffer();
    	while ((inputLine = in.readLine()) != null) {
    		System.out.println("inputLine: "+inputLine);
    		System.out.println("inputLine1: "+inputLine1);
    	    content.append(inputLine);
    	    inputLine1 += inputLine+'\n';
    	}
    	in.close();

        return new ResponseEntity<String> (inputLine1, HttpStatus.OK);
    }	
	@RequestMapping(value="/text/{folderId}/{textName}")
    public ResponseEntity<String> getTextByFolderId(@PathVariable("folderId") String folderId,
    		@PathVariable("textName") String textName) throws IOException{
		System.out.println("/text/{folderId}/{textName} accessed with folderId "+ folderId +" and textName " + textName);

		String fileId = GetFileIdInFolder(folderId, textName);
		if(fileId != "") {
	    	URL url = new URL("https://api.box.com/2.0/files/"+fileId+"/content?"+acc_tok);
	    	HttpURLConnection con = (HttpURLConnection) url.openConnection();
	    	con.setRequestMethod("GET");
	    	con.setConnectTimeout(15 * 1000);
	    	
	    	BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
	    	String inputLine = "";
	    	String inputLine1 = "";
	    	StringBuffer content = new StringBuffer();
	    	while ((inputLine = in.readLine()) != null) {
	    		System.out.println("inputLine1: " + inputLine1);
	    	    content.append(inputLine);
	    	    inputLine1 += inputLine+'\n';
	    	}
	    	in.close();
    		System.out.println("final inputLine1: " + inputLine1);
	        return new ResponseEntity<String> (inputLine1, HttpStatus.OK);
		}
        return new ResponseEntity<String> ("", HttpStatus.NOT_FOUND);
    }
	
	@RequestMapping(value="/textArr/{id}")
    public ResponseEntity<String> getTextArrById(@PathVariable("id") String id) throws IOException{
		System.out.println("/textArr/id accessed with id "+ id +" at url https://api.box.com/2.0/files/"+id+"?"+acc_tok);
    	URL url = new URL("https://api.box.com/2.0/files/"+id+"/content?"+acc_tok);

    	HttpURLConnection con = (HttpURLConnection) url.openConnection();
    	con.setRequestMethod("GET");
    	con.setConnectTimeout(15 * 1000);
    	
    	BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
    	String inputLine = "";
    	String inputLine1 = "";
    	while ((inputLine = in.readLine()) != null) {
    		System.out.println("inputLine: "+inputLine);
    		System.out.println("inputLine1: "+inputLine1);
    		if(inputLine != "") {
    			inputLine1 += inputLine+';';
    		}
    	}
		System.out.println("final inputLine1: "+inputLine1.substring(0, inputLine1.length()-1));
    	in.close();

    	// We cut off the very last ';' because this would create an empty entry in our array on the C# side
        return new ResponseEntity<String> (inputLine1.substring(0, inputLine1.length()-1), HttpStatus.OK);
    }
	
	@RequestMapping(value="/textArr/{folderId}/{textArrName}")
    public ResponseEntity<String> getTextArrByFolderId(@PathVariable("folderId") String folderId,
    		@PathVariable("textArrName") String textArrName) throws IOException{
		System.out.println("/textArr/{folderId}/{textArrName} accessed with folderId "
											+ folderId +" and textArrName "+textArrName);
		
		String fileId = GetFileIdInFolder(folderId, textArrName);
		System.out.println("https://api.box.com/2.0/files/"+fileId+"/content?"+acc_tok);
		if(fileId != "") {
	    	URL url = new URL("https://api.box.com/2.0/files/"+fileId+"/content?"+acc_tok);
	
	    	HttpURLConnection con = (HttpURLConnection) url.openConnection();
	    	con.setRequestMethod("GET");
	    	con.setConnectTimeout(15 * 1000);
	    	
	    	BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
	    	String inputLine = "";
	    	String inputLine1 = "";
	    	while ((inputLine = in.readLine()) != null) {
	    		if(inputLine != "") {
	    			inputLine1 += inputLine+';';
	    		}
	    	}
	    	in.close();
	
	    	// We cut off the very last ';' because this would create an empty entry in our array on the C# side
	        return new ResponseEntity<String> (inputLine1.substring(0, inputLine1.length()-1), HttpStatus.OK);
		}
        return new ResponseEntity<String> ("", HttpStatus.NOT_FOUND);
    }
	
	@RequestMapping(value="/bulkInitialLoad")
    public ResponseEntity<String> getBulkInitialLoad() throws IOException{
		
		// Get the id of the Games_List folder
		String gamesListFileId = GetFileIdInFolder("0", "Game_Files");
		
		// Get all of the game files under the Games_List folder
		JSONArray ja = getFolderInfoAsJsonArray(gamesListFileId);
    	String inputLine = "";
    	for(int ii=0; ii<1; ++ii) {
    		JSONObject joEntry= new JSONObject();
    		joEntry = ja.getJSONObject(ii);
    		joEntry.remove("etag");
    		joEntry.remove("type");
    		joEntry.remove("sequence_id");
    		//joEntry.put("Banner", GetFileIdInFolder(joEntry.getString("id"), "Banner"));
    		joEntry.put("Banner", GetImgDataAsBytesInFolder(joEntry.getString("id"), "Banner"));
    		joEntry.put("categories", getTextArrByFolderIdAndFileName(joEntry.getString("id"), "Tags"));
        	//System.out.println("joEntry: " + joEntry.toString());
    		inputLine += joEntry.toString() + ";";
    	}	
    	//System.out.println("inputLine: " + inputLine);

    	// We cut off the very last ';' because this would create an empty entry in our array on the C# side
        return new ResponseEntity<String> (inputLine.substring(0, inputLine.length()-1), HttpStatus.OK);
    }
	
	@RequestMapping(value="/bulkInitialloadforpurejs")
    public ResponseEntity<String> getBulkInitialLoadForPureJs() throws IOException{
		
		// Get the id of the Games_List folder
		String gamesListFileId = GetFileIdInFolder("0", "Game_Files");
		
		// Get all of the game files under the Games_List folder
		JSONArray ja = getFolderInfoAsJsonArray(gamesListFileId);
    	String inputLine = "";
    	for(int ii=0; ii<1; ++ii) {
    		JSONObject joEntry= new JSONObject();
    		joEntry = ja.getJSONObject(ii);
    		joEntry.remove("etag");
    		joEntry.remove("type");
    		joEntry.remove("sequence_id");
    		joEntry.put("Banner", getSharedUrlByFileId(GetFileIdInFolder(joEntry.getString("id"), "Banner")));
    		System.out.println("Banner shared_url: " + joEntry.get("Banner"));
    		joEntry.put("categories", getTextArrByFolderIdAndFileName(joEntry.getString("id"), "Tags"));
        	//System.out.println("joEntry: " + joEntry.toString());
    		inputLine += joEntry.toString() + ";";
    	}	
    	//System.out.println("inputLine: " + inputLine);

    	// We cut off the very last ';' because this would create an empty entry in our array on the js side
        return new ResponseEntity<String> (inputLine.substring(0, inputLine.length()-1), HttpStatus.OK);
    }
	
	@RequestMapping(value="/gamedata/{folderId}")
    public ResponseEntity<String> gameDataByFolderId(@PathVariable("folderId") String folderId) throws IOException{

		System.out.println("gameDataByFolderId with id: " + folderId);
		// Get all of the game files under the Games_List folder
		JSONArray ja = getFolderInfoAsJsonArray(folderId);
		JSONObject finalJsonObject= new JSONObject();
    	for(int ii=0; ii<ja.length(); ++ii) {
    		JSONObject joEntry= new JSONObject();
    		joEntry = ja.getJSONObject(ii);
    		String justName = getNameWithoutExt(joEntry.getString("name"));
    		System.out.println("Found: " + joEntry.getString("name") +" for item " +ii);
    		if(justName.toLowerCase().contains("tags") || justName.toLowerCase().contains("banner") || justName.toLowerCase().contains(".meta"))
    		{ 
        		System.out.println("Skipping " + justName);
    			// Do nothing, skip this
    		}
    		else if(joEntry.getString("name").contains(".mp4") && joEntry.has("shared_link"))
    		{ 
    			String vidUrl = getSharedUrlByFileId(joEntry.getString("id"));
    			finalJsonObject.put(justName, vidUrl);
        		System.out.println("Adding " + justName +" with value "+ vidUrl);
    		}
    		else if(joEntry.getString("name").contains(".jpg") || joEntry.getString("name").contains(".png")) {
    			finalJsonObject.put(justName, joEntry.getString("id"));
        		System.out.println("Adding " + justName + " with value " + finalJsonObject.getString(justName));
    		}
    		else if(joEntry.getString("name").contains(".txt")) {
    			finalJsonObject.put(justName, GetTxtDataByFileId(joEntry.getString("id")));
        		System.out.println("Adding " + justName +" with value "+ GetTxtDataByFileId(joEntry.getString("id")));
    		}
    	}	
        return new ResponseEntity<String> (finalJsonObject.toString(), HttpStatus.OK);
    }

	public String getNameExt(String name) {
		return name.substring(name.length()-3, name.length());
	}
	public String getNameWithoutExt(String name) {
		return name.substring(0, name.length()-4);
	}
	
    public String[] getTextArrByFolderIdAndFileName(String folderId, String fileName) throws IOException{
    	
    	String fileId = GetFileIdInFolder(folderId, fileName); 
    	
		System.out.println("getTextArrByFolderIdAndFileName accessed with id "+ fileId + " and fileName " + fileName);
    	URL url = new URL("https://api.box.com/2.0/files/"+fileId+"/content?"+acc_tok);

    	HttpURLConnection con = (HttpURLConnection) url.openConnection();
    	con.setRequestMethod("GET");
    	con.setConnectTimeout(15 * 1000);
    	
    	BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
    	String inputLine = "";
    	List<String> inputLine1 = new ArrayList<String>();
    	while ((inputLine = in.readLine()) != null) {
    		System.out.println("inputLine: "+inputLine);
    		System.out.println("inputLine1: "+inputLine1);
    		if(inputLine != "") {
    			inputLine1.add(inputLine);
    		}
    	}
    	in.close();
    	String[] output = new String[inputLine1.size()];
    	for(int ii=0; ii<inputLine1.size(); ++ii) {
        	output[ii] = inputLine1.get(ii);
    	}
        return output;
    }
	
    public String getSharedUrlByFileId(String fileId) throws IOException{
    	
		System.out.println("getSharedUrlByFileId fn accessed with id "+ fileId);
    	URL url = new URL("https://api.box.com/2.0/files/"+fileId+"?"+acc_tok);

    	HttpURLConnection con = (HttpURLConnection) url.openConnection();
    	con.setRequestMethod("GET");
    	con.setConnectTimeout(15 * 1000);
    	
    	BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
		System.out.println("getSharedUrlByFileId fn BufferedReader in: "+ in.toString());
    	JSONObject jo = new JSONObject();
    	String inputLine = "";
    	while ((inputLine = in.readLine()) != null) {
    		System.out.println("getSharedUrlByFileId inputLine: "+inputLine);
    		jo = new JSONObject(inputLine);
    	}
    	
    	if(jo.has("shared_link")) {
        	JSONObject joLink = new JSONObject(jo.get("shared_link"));
        	if(joLink.has("url")) {
            	in.close();
        		return joLink.getString("url");
        	}
        	else {
        		System.out.println("getSharedUrlByFileId fn shared_link == null");
        		System.out.println("getSharedUrlByFileId fn attempting to modify share_link");
        		JSONObject json_permissions = new JSONObject();
        		json_permissions.put("can_download", true);
        		json_permissions.put("can_preview", true);
        		
        		JSONObject json_access = new JSONObject();
        		json_access.put("access", "open");
        		json_access.put("effective_access", "open");
        		json_access.put("effective_permission", "can_download");
        		json_access.put("is_password_enabled", "false");
        		json_access.put("permissions", json_permissions);
        		
        		
            	HttpURLConnection con_put = (HttpURLConnection) url.openConnection();
            	con_put.setRequestMethod("PUT");
            	con_put.setConnectTimeout(15 * 1000);
            	//con_put.setRequest("shared_link", json_access.toString());
            	con_put.setDoOutput(true);
            	OutputStream os = con_put.getOutputStream();
            	
            	os.write(json_access.toString().getBytes());
        		
        		/*BoxAPIConnection api = new BoxAPIConnection(acc_tok_standalone);

        		BoxFile file = new BoxFile(api, "id");
				BoxSharedLink.Permissions permissions = new BoxSharedLink.Permissions();
				permissions.setCanDownload(true);
				permissions.setCanPreview(true);
				BoxSharedLink sharedLink = file.createSharedLink(BoxSharedLink.Access.OPEN, null, permissions);
				*/
        		return ""; //sharedLink.getURL();
        	}
    	}
    	in.close();

        return "Not Found";
    }
	
	@RequestMapping(value="/folderInfo/{folderId}")
    public ResponseEntity<String> getFolderInfoById(@PathVariable("folderId") String folderId) throws IOException{
		System.out.println("/folderInfo/id accessed with id "+ folderId +" at url https://api.box.com/2.0/folders/"+folderId+"/items?"+acc_tok);
    	URL url = new URL("https://api.box.com/2.0/folders/"+folderId+"/items?"+acc_tok);

    	HttpURLConnection con = (HttpURLConnection) url.openConnection();
    	con.setRequestMethod("GET");
    	con.setConnectTimeout(15 * 1000);
    	
    	BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
    	System.out.println(in.toString());
    	JSONObject jo = new JSONObject();
    	String inputLine = "";
    	String inputLine1 = "";
    	while ((inputLine = in.readLine()) != null) {
    		System.out.println("inputLine: "+inputLine);
    		System.out.println("inputLine1: "+inputLine1);
    		jo = new JSONObject(inputLine);
    	}
    	if(!jo.isNull("total_count")) {
    		JSONArray ja = jo.getJSONArray("entries");
	    	for(int ii=0; ii<jo.getInt("total_count"); ++ii) {
	    		JSONObject joEntry= new JSONObject();
	    		joEntry = ja.getJSONObject(ii);
	    		System.out.println("item "+joEntry.getString("name")+": "+joEntry.getString("id"));
	    		inputLine1 += joEntry.getString("name")+";"+joEntry.getString("id")+";";
	    	}
    	}
    	in.close();

        return new ResponseEntity<String> (inputLine1, HttpStatus.OK);
    }
	@RequestMapping(value="/folderInfoAsJson/{folderId}")
    public ResponseEntity<JSONObject> getFolderInfoAsJsonById(@PathVariable("folderId") String folderId) throws IOException{
		System.out.println("/folderInfo/id accessed with id "+ folderId +" at url https://api.box.com/2.0/folders/"+folderId+"/items?"+acc_tok);
    	URL url = new URL("https://api.box.com/2.0/folders/"+folderId+"/items?"+acc_tok);

    	HttpURLConnection con = (HttpURLConnection) url.openConnection();
    	con.setRequestMethod("GET");
    	con.setConnectTimeout(15 * 1000);
    	
    	BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
    	System.out.println(in.toString());
    	JSONObject jo = new JSONObject();
    	String inputLine = "";
    	String inputLine1 = "";
    	JSONObject jsonOutput = new JSONObject();
    	while ((inputLine = in.readLine()) != null) {
    		System.out.println("inputLine: "+inputLine);
    		System.out.println("inputLine1: "+inputLine1);
    		jo = new JSONObject(inputLine);
    	}
    	if(!jo.isNull("total_count")) {
    		JSONArray ja = jo.getJSONArray("entries");
	    	for(int ii=0; ii<jo.getInt("total_count"); ++ii) {
	    		//JSONObject joEntry= new JSONObject();
	    		jsonOutput.append(ja.getJSONObject(ii).getString("name"), ja.getJSONObject(ii).getString("id"));
	    		System.out.println("item "+ja.getJSONObject(ii).getString("name")+": "+jsonOutput.getString(ja.getJSONObject(ii).getString("name")));
	    	}
    	}
    	in.close();

        return new ResponseEntity<JSONObject> (jsonOutput, HttpStatus.OK);
    }
	
    public String GetFileIdInFolder(String folderId, String fileName) throws IOException {
    	
    	try {
			URL url = new URL("https://api.box.com/2.0/folders/"+folderId+"/items?"+acc_tok);
	    	HttpURLConnection con = (HttpURLConnection) url.openConnection();
	    	con.setRequestMethod("GET");
	    	con.setConnectTimeout(15 * 1000);
	    	BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
	    	String inputLine = "";
			JSONObject jo = new JSONObject();
			while ((inputLine = in.readLine()) != null) {
				jo = new JSONObject(inputLine);
			}
			if(!jo.isNull("total_count")) {
				JSONArray ja = jo.getJSONArray("entries");
		    	for(int ii=0; ii<jo.getInt("total_count"); ++ii) {
	    			String nameWithoutExt = "";
	    			System.out.println("GetFileIdInFolder, getting file " + ja.getJSONObject(ii).getString("name"));
	    			if(ja.getJSONObject(ii).getString("name").contains(".") && !ja.getJSONObject(ii).getString("name").contains(".meta"))
	    				nameWithoutExt = ja.getJSONObject(ii).getString("name")
	        					.substring(0, ja.getJSONObject(ii).getString("name").length()-4);
	    			else
	    				nameWithoutExt = ja.getJSONObject(ii).getString("name");
	    			//System.out.println("Comparing "+nameWithoutExt+ " to " + fileName);
		    		if(nameWithoutExt.toLowerCase().equals(fileName.toLowerCase())) {
						return ja.getJSONObject(ii).getString("id");
		    		}
		    	}
			}	
    	}
    	catch(Exception e) {
    		return "";
    	}
		return "";
    }
	
    public String GetImgNameWithExtInFolder(String folderId, String fileName) throws IOException { 
    	
		URL url = new URL("https://api.box.com/2.0/folders/"+folderId+"/items?"+acc_tok);
    	HttpURLConnection con = (HttpURLConnection) url.openConnection();
    	con.setRequestMethod("GET");
    	con.setConnectTimeout(15 * 1000);
    	BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));	
    	String inputLine = "";
		JSONObject jo = new JSONObject();
		while ((inputLine = in.readLine()) != null) {
			jo = new JSONObject(inputLine);
		}
		if(!jo.isNull("total_count")) {
			JSONArray ja = jo.getJSONArray("entries");
	    	for(int ii=0; ii<jo.getInt("total_count"); ++ii) {
    			String nameWithExt = "";
    			System.out.println("GetImgExtInFolder, getting file " + ja.getJSONObject(ii).getString("name")); 
    			nameWithExt = ja.getJSONObject(ii).getString("name");
    			if(nameWithExt.contains(fileName)) {
	    			System.out.println(nameWithExt);
	    			return nameWithExt;
    			}
	    	}
		}
		return "";
    }
	
    public byte[] GetImgDataInFolder(String folderId, String imgName) throws IOException { 
		System.out.println("/pic/{folderId}/{imgName} accessed with folderId "+ folderId +" and imgName " + imgName);
	
		String fileId = GetFileIdInFolder(folderId, imgName);
		if(fileId != "") {
	    	URL imgUrl = new URL("https://api.box.com/2.0/files/"+fileId+"/content?"+acc_tok);
	    	BufferedImage dImg = ImageIO.read(imgUrl);
			String imgWithExt = GetImgNameWithExtInFolder(folderId, imgName); 
			System.out.println("img name is "+ imgWithExt);
	    	File img;
	    	if(imgWithExt.contains(".jpg")) {
	    		img = new File("downloaded.jpg");
	    		ImageIO.write(dImg, "jpg", img);
	    	}
	    	else {
	    		img = new File("downloaded.png");
	    		ImageIO.write(dImg, "png", img);
	    	}
	
	        return Files.readAllBytes(img.toPath());
		}
		return new byte[0];
    }	
	
    public byte[] GetImgDataAsBytesInFolder(String folderId, String imgName) throws IOException { 
		System.out.println("GetImgDataAsBytesInFolder accessed with folderId "+ folderId +" and imgName " + imgName);
	
		String fileId = GetFileIdInFolder(folderId, imgName);
		if(fileId != "") {
	    	URL imgUrl = new URL("https://api.box.com/2.0/files/"+fileId+"/content?"+acc_tok);

	    	ByteArrayOutputStream baos = new ByteArrayOutputStream();
	    	InputStream inputStrm = imgUrl.openStream();
	    	byte[] imgBuffer = new byte[4096];
	    	int bytesRead = -1;
	    	while((bytesRead = inputStrm.read(imgBuffer)) > 0) {
	    		baos.write(imgBuffer);
	    	}

	        return baos.toByteArray();
		}
		return new byte[0];
    }	
   
	
    public byte[] GetImgDataByFileIdAndExt(String fileId, String ext) throws IOException { 
		System.out.println("GetImgDataByFileIdAndExt accessed with id "+ fileId +" and ext " + ext);
	
		if(fileId != "") {
	    	URL imgUrl = new URL("https://api.box.com/2.0/files/"+fileId+"/content?"+acc_tok);
	    	BufferedImage dImg = ImageIO.read(imgUrl);
	    	File img;
	    	if(ext.contains(".jpg")) {
	    		img = new File("downloaded.jpg");
	    		ImageIO.write(dImg, "jpg", img);
	    	}
	    	else {
	    		img = new File("downloaded.png");
	    		ImageIO.write(dImg, "png", img);
	    	}
	
	        return Files.readAllBytes(img.toPath());
		}
		return new byte[0];
    }	
	
    public String GetTxtDataByFileId(String fileId) throws IOException { 
		System.out.println("GetTxtDataByFileId accessed with id "+ fileId);
	
		if(fileId != "") {
	    	URL url = new URL("https://api.box.com/2.0/files/"+fileId+"/content?"+acc_tok);
	    	HttpURLConnection con = (HttpURLConnection) url.openConnection();
	    	con.setRequestMethod("GET");
	    	con.setConnectTimeout(15 * 1000);
	    	
	    	BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
	    	String inputLine = "";
	    	String inputLine1 = "";
	    	StringBuffer content = new StringBuffer();
	    	while ((inputLine = in.readLine()) != null) {
	    		System.out.println("inputLine1: " + inputLine1);
	    	    content.append(inputLine);
	    	    inputLine1 += inputLine+'\n';
	    	}
	    	in.close();
    		System.out.println("final inputLine1: " + inputLine1);
	        return inputLine1;
		}
        return "";
    }	
    
    public JSONArray getFolderInfoAsJsonArray(String folderId) throws IOException{
		System.out.println("/folderInfo/id accessed with id "+ folderId +" at url https://api.box.com/2.0/folders/"+folderId+"/items?"+acc_tok);
    	URL url = new URL("https://api.box.com/2.0/folders/"+folderId+"/items?"+acc_tok);

    	HttpURLConnection con = (HttpURLConnection) url.openConnection();
    	con.setRequestMethod("GET");
    	con.setConnectTimeout(15 * 1000);
    	
    	BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
    	System.out.println(in.toString());
    	JSONObject jo = new JSONObject();
    	String inputLine = "";
    	String inputLine1 = "";
    	while ((inputLine = in.readLine()) != null) {
    		System.out.println("inputLine: "+inputLine);
    		System.out.println("inputLine1: "+inputLine1);
    		jo = new JSONObject(inputLine);
    	}
    	JSONArray ja = new JSONArray();
    	if(!jo.isNull("total_count")) {
    		ja = jo.getJSONArray("entries");
    	}
    	in.close();

        return ja;
    }
	/*
	@RequestMapping(value="/p")
    public ResponseEntity<byte[]> getImage() throws IOException{
		System.out.println("/p accessed at url https://api.box.com/2.0/files/586561856104?"+acc_tok);
    	URL url = new URL("https://api.box.com/2.0/files/586561856104/content?"+acc_tok);
    	BufferedImage dImg = ImageIO.read(url);
    	File img = new File("downloaded.jpg");
    	ImageIO.write(dImg, "jpg", img);
        return ResponseEntity.ok().contentType(MediaType.valueOf(FileTypeMap.getDefaultFileTypeMap().getContentType(img))).body(Files.readAllBytes(img.toPath()));

    	 //WritableRaster raster = dImg .getRaster();
    	 //DataBufferByte data   = (DataBufferByte) raster.getDataBuffer();
    	//return data.getData();
    }*/
    
	@RequestMapping(value="/pic")
	public ResponseEntity<byte[]> picPage() throws IOException {

		System.out.println("/pic accessed at url https://api.box.com/2.0/files/604116470436?"+acc_tok);		
		String fileId = "604116470436";
		if(fileId != "") {
	    	byte[] imgBuffer = GetImgDataAsBytesInFolder("101200716701", "Banner");
	        //return baos.toByteArray();
	        return new ResponseEntity<byte[]>(imgBuffer, HttpStatus.OK);
		}

        return new ResponseEntity<byte[]>(new byte[0], HttpStatus.OK);
		//return new byte[0];
		//Toolkit toolkit = Toolkit.getDefaultToolkit();
		//MediaTracker tracker = new MediaTracker(null);
		//tracker.addImage(image, 0);
		//tracker.waitForAll();    	
		//Image image = null;
        /*try {
        	URL url = new URL("https://api.box.com/2.0/files/586561856104?"+acc_tok);
            image = ImageIO.read(url);
        } catch (IOException e) {
        	e.printStackTrace();
        }*/
        
        //File img = new File("https://api.box.com/2.0/files/586561856104?access_token=tMCCSaYlSCAx611LGfpcopdZqWLv19f9");
        //byte[] arr = MediaType.valueOf(FileTypeMap.getDefaultFileTypeMap().getContentType(img))).body(Files.readAllBytes(img.toPath());

        //JFrame frame = new JFrame();
        //frame.setSize(300, 300);
        //JLabel label = new JLabel();//(new ImageIcon(image));
        //frame.add(label);
        //frame.setVisible(true);

        //return ResponseEntity.ok().contentType(MediaType.IMAGE_JPEG).body(image);

	}
}
