package vertx.auth;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Hashtable;
import java.util.Properties;

import io.vertx.core.json.JsonObject;

/**
 * This class will be used as a util class to authenticate a user by its login/password (in clear)
 * 	Purpose of this class is just to give a corresponding response, not really to work on VertX
 *
 */
public class AuthenticatorService {

	/* a list of login/password */
	private static Hashtable<String, String> listUser = new Hashtable<String, String>();
	/* the file where are the credentials to be read */
	private final static String fileCredentials = "users.properties";

	/**
	 * Statically we'll prepare the list of credentials from the file
	 */
	static {
		try {
			Properties prop = new Properties();
			String propFileName = fileCredentials;
			InputStream inputStream = AuthenticatorService.class.getClassLoader().getResourceAsStream(propFileName);
			if (inputStream != null) {
				prop.load(inputStream);
				for (String key : prop.stringPropertyNames()) {
					String value = prop.getProperty(key);
					listUser.put(key, value);
				}
			} else {
				throw new FileNotFoundException("property file '" + propFileName + "' not found in the classpath");
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * We'll verify if the given credentials are part of the list of user (from the file)
	 * @param myRequest
	 * @return true if the credential are found or false otherwise 
	 */
	public static boolean checkAuthent(JsonObject myRequest) {
		String login = myRequest.getString("login");
		String password = myRequest.getString("password");
		System.out.println("Check authent for [" + login + "]/[" + password + "]");
		return (AuthenticatorService.getListUser().containsKey(login)
				&& AuthenticatorService.getListUser().get(login).equals(password));
	}
	
	public static Hashtable<String, String> getListUser() {
		return listUser;
	}
}
