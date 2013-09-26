package com.TentacleLabs.GoogleAnalyticsPlugin;

import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

import org.bukkit.plugin.Plugin;

/**
 * Defines a Google Analytics tracker.
 * 
 * @author Oliver
 *
 */
public class Tracker {
	private static final String VERSION = "4.4sj";
	private static final String UTM_GIF_LOCATION = "http://www.google-analytics.com/__utm.gif";

	private final ExecutorService executor = Executors.newCachedThreadPool();
	
	private final String analyticsServerDomain;
	private final String analyticsServerAccount;
	
	private final Plugin plugin;
	private final boolean enableDebug;
	
	
	/**
	 * Create a new instance of a tracker.
	 * @param analyticsServerDomain The domain name of the google analytics account.
	 * @param analyticsServerAccount Th account id of the google analytics account.
	 * @param enableDebug Enable outputting tracking urls.
	 */
	public Tracker(Plugin plugin, String analyticsServerDomain, String analyticsServerAccount, boolean enableDebug) {
		this.plugin = plugin;
		this.enableDebug = enableDebug;
		
		this.analyticsServerDomain = analyticsServerDomain;
		this.analyticsServerAccount = analyticsServerAccount;
	}


	/**
	 * Track something as action an pageview.
	 * @param clientName
	 * @param visitorId
	 * @param visitorIp
	 * @param category
	 * @param action
	 * @param label
	 */
	public void Track(String clientName, String visitorId, String visitorIp, String category, String action, String label) {
		executor.execute(new TrackPageActionExecuterTask(clientName, visitorId, visitorIp, null, null, category, action, label)); 
		executor.execute(new TrackPageViewExecuterTask(clientName, visitorId, visitorIp, category, action + "&" + label)); 
	}
	
	/**
	 * Track an action.
	 * @param clientName
	 * @param visitorId
	 * @param visitorIp
	 * @param category
	 * @param action
	 * @param label
	 */
	public void TrackAction(String clientName, String visitorId, String visitorIp, String category, String action, String label) {
		executor.execute(new TrackPageActionExecuterTask(clientName, visitorId, visitorIp, null, null, category, action, label)); 
	}
	
	/**
	 * Track a pageview.
	 * @param clientName
	 * @param visitorId
	 * @param visitorIp
	 * @param action
	 * @param query
	 */
	public void TrackView(String clientName, String visitorId, String visitorIp, String action, String query) {
		executor.execute(new TrackPageViewExecuterTask(clientName, visitorId, visitorIp, action, query)); 
	}
	
	
	private static boolean isEmpty(String in) {
		return in == null || "-".equals(in) || "".equals(in);
	}
	
	private static String getRandomNumber() {
		return Integer.toString((int) (Math.random() * 0x7fffffff));
	} 
	
	private static String getIP(String remoteAddress) {
		if (isEmpty(remoteAddress)) {
			return "";
		}

		String regex = "^([^.]+\\.[^.]+\\.[^.]+\\.).*";
		Pattern getFirstBitOfIPAddress = Pattern.compile(regex);
		Matcher m = getFirstBitOfIPAddress.matcher(remoteAddress);
		
		if (m.matches()) {
			return m.group(1) + "0";
		} else {
			return "";
		}
	}
	
	private static String getVisitorId(String visitorId, String account) throws NoSuchAlgorithmException, UnsupportedEncodingException {
		String message = account + " " + visitorId;
		
		MessageDigest m = MessageDigest.getInstance("MD5");
		m.update(message.getBytes("UTF-8"), 0, message.length());
		byte[] sum = m.digest();
		BigInteger messageAsNumber = new BigInteger(1, sum);
		String md5String = messageAsNumber.toString(16);

		// Pad to make sure id is 32 characters long.
		while (md5String.length() < 32) {
		  md5String = "0" + md5String;
		}

		return "0x" + md5String.substring(0, 16);
	}
  
	
	private void trackPageAction(String clientName, String visitorId, String visitorIp, String url, String query, String category, String action, String label) throws Exception {
		String domainName = analyticsServerDomain;
		
		if (isEmpty(domainName)) {
			domainName = "";
		}

		String documentReferer = "-";
		String documentPath = url;
		
		if (documentPath != null) {
			if (query != null) {
				documentPath += "?" + query;
			}
		}
		      
		if (isEmpty(documentPath)) {
		  documentPath = "";
		} else {
		  documentPath = URLDecoder.decode(documentPath, "UTF-8");
		}
		
		String event = "(" + category + "*" + action + "*" + label + ")";		

		// Construct the gif hit url.
		String utmUrl = UTM_GIF_LOCATION + "?" +
		"utmwv=" + VERSION +
		"&utmn=" + getRandomNumber() +
		"&utmhn=" + URLEncoder.encode(domainName, "UTF-8") +
		"&utmr=" + URLEncoder.encode(documentReferer, "UTF-8") +
		"&utmp=" + URLEncoder.encode(documentPath, "UTF-8") +
		"&utmt=" + "event" +
		"&utme=" + URLEncoder.encode("5" + event, "UTF-8").replace("+", "%20") + 
		"&utmac=" + analyticsServerAccount +
		"&utmcc=__utma%3D999.999.999.999.999.1%3B" +
		"&utmvid=" + getVisitorId(visitorId, analyticsServerAccount) +
        "&utmip=" + getIP(visitorIp);
		
		if(enableDebug) {
			plugin.getLogger().info("Tracker Url: " + utmUrl);
		}
				
		sendRequestToGoogleAnalytics(utmUrl, clientName);
	}
	
	private void trackPageView(String clientName, String visitorId, String visitorIp, String url, String query) throws Exception {
		String domainName = analyticsServerDomain;
		
		if (isEmpty(domainName)) {
			domainName = "";
		}

		String documentReferer = "-";
		String documentPath = url;
		
		if (documentPath != null) {
			if (query != null) {
				documentPath += "?" + query;
			}
		}
		      
		if (isEmpty(documentPath)) {
		  documentPath = "";
		} else {
		  documentPath = URLDecoder.decode(documentPath, "UTF-8");
		}

		// Construct the gif hit url.
		String utmUrl = UTM_GIF_LOCATION + "?" +
		"utmwv=" + VERSION +
		"&utmn=" + getRandomNumber() +
		"&utmhn=" + URLEncoder.encode(domainName, "UTF-8") +
		"&utmr=" + URLEncoder.encode(documentReferer, "UTF-8") +
		"&utmp=" + URLEncoder.encode(documentPath, "UTF-8") +
		"&utmac=" + analyticsServerAccount +
		"&utmcc=__utma%3D999.999.999.999.999.1%3B" +
		"&utmvid=" + getVisitorId(visitorId, analyticsServerAccount) +
        "&utmip=" + getIP(visitorIp);

		if(enableDebug) {
			plugin.getLogger().info("Tracker Url: " + utmUrl);
		}
		
		sendRequestToGoogleAnalytics(utmUrl, clientName);
	}
	
	private void sendRequestToGoogleAnalytics(String utmUrl, String clientName) throws Exception {
		try {
			URL url = new URL(utmUrl);
			URLConnection connection = url.openConnection();
			connection.setUseCaches(false);
			connection.setRequestProperty("User-Agent", clientName);
			connection.connect();
			
			InputStream in = connection.getInputStream();
			int available;
					
			while((available = in.available()) != 0)
			{
				in.skip(available);			
			}
			
			in.close();
	  	} catch (Exception e) {
	  		plugin.getLogger().warning("Tracker Connection Error: " + e.getMessage());
	  	}
	}	


	private class TrackPageActionExecuterTask implements Runnable {
		private String clientName;
		private String visitorId;
		private String visitorIp;
		private String url; 
		private String query; 
		private String category;
		private String action; 
		private String label;
		
		
		public TrackPageActionExecuterTask(String clientName, String visitorId, String visitorIp, String url, String query, String category, String action, String label) {
			this.clientName = clientName;
			this.visitorId = visitorId;
			this.visitorIp = visitorIp;
			this.url = url;
			this.query = query;
			this.category = category;
			this.action = action;
			this.label = label;
		}
		
		@Override public void run() { 
			try {
				trackPageAction(clientName, visitorId, visitorIp, url, query, category, action, label);
			} catch (Exception e) {
				plugin.getLogger().warning("Tracker Error: " + e.getMessage());
			}
		}
	}
	
	private class TrackPageViewExecuterTask implements Runnable {
		private String clientName;
		private String visitorId;
		private String visitorIp;
		private String url; 
		private String query; 
		
		
		public TrackPageViewExecuterTask(String clientName, String visitorId, String visitorIp, String url, String query) {
			this.clientName = clientName;
			this.visitorId = visitorId;
			this.visitorIp = visitorIp;
			this.url = url;
			this.query = query;
		}
		
		@Override public void run() { 
			try {
				trackPageView(clientName, visitorId, visitorIp, url, query);
			} catch (Exception e) {
				plugin.getLogger().warning("Tracker Error: " + e.getMessage());
			}
		}
	}
}