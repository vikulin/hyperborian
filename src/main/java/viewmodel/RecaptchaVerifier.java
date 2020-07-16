package viewmodel;

import java.io.*;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

import org.zkoss.json.*;


public class RecaptchaVerifier {

	final static String VERIFY_URL = "https://www.google.com/recaptcha/api/siteverify";
	/**
	 * Refer to https://developers.google.com/recaptcha/docs/verify
	 * You can simplify this method with Apache HttpClient library.
	 * @param captchaResponse
	 * @return a JSON object
	 * {
	 *  "success": true|false,
	 *  "challenge_ts": timestamp,  // timestamp of the challenge load (ISO format yyyy-MM-dd'T'HH:mm:ssZZ)
	 *  "hostname": string,         // the hostname of the site where the reCAPTCHA was solved
	 *  "error-codes": [...]        // optional
	 *  }
	 * @throws Exception
	 */
	public static JSONObject verifyResponse(String secret, String captchaResponse) throws Exception {

		URL obj = new URL(VERIFY_URL);
		HttpsURLConnection con = (HttpsURLConnection) obj.openConnection();

		//add header
		con.setRequestMethod("POST");
		con.setRequestProperty("Accept-Language", "en-US,en;q=0.5");
		String urlParameters = "response="+captchaResponse+"&secret="+secret;

		// Send a request
		con.setDoOutput(true);
		DataOutputStream wr = new DataOutputStream(con.getOutputStream());
		wr.writeBytes(urlParameters);
		wr.flush();
		wr.close();

		InputStreamReader in = new InputStreamReader(con.getInputStream());

		JSONObject result = (JSONObject)JSONValue.parse(in);
		in.close();

		return result;
	}
}