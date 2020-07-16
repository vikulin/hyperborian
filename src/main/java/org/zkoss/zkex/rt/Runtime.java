package org.zkoss.zkex.rt;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.Calendar;
import java.util.Date;
import java.util.prefs.Preferences;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zkoss.io.Files;
import org.zkoss.json.JSONObject;
import org.zkoss.json.JSONValue;
import org.zkoss.json.JSONs;
import org.zkoss.lang.Classes;
import org.zkoss.lang.Library;
import org.zkoss.util.resource.Locator;
import org.zkoss.util.resource.Locators;
import org.zkoss.zk.au.AuResponse;
import org.zkoss.zk.au.AuService;
import org.zkoss.zk.au.out.AuAlert;
import org.zkoss.zk.ui.Execution;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.Session;
import org.zkoss.zk.ui.WebApp;
import org.zkoss.zk.ui.WebApps;
import org.zkoss.zk.ui.impl.AbstractWebApp;
import org.zkoss.zk.ui.util.Clients;
import org.zkoss.zkex.license.CipherParam;
import org.zkoss.zkex.license.KeyStoreParam;
import org.zkoss.zkex.license.LicenseParam;
import org.zkoss.zkex.util.Base64Coder;
import org.zkoss.zkex.util.ObfuscatedString;

public final class Runtime {
	public static final String COMPANY_NAME;
	public static final String COMPANY_ADDRESS;
	public static final String COMPANY_ZIPCODE;
	public static final String COUNTRY;
	public static final String PROJECT_NAME;
	public static final String PRODUCT_NAME;
	public static final String PACKAGE;
	public static final String VERSION;
	public static final String ISSUE_DATE;
	public static final String EXPIRY_DATE;
	public static final String TERM;
	public static final String VERIFICATION_NUMBER;
	public static final String INFORMATION;
	public static final String KEY_SIGNATURE;
	public static final String CHECK_PERIOD;
	public static final String LICENSE_DIRECTORY_PROPERTY;
	public static final String LICENSE_VERSION;
	public static final String WARNING_EXPIRY;
	public static final String WARNING_PACKAGE;
	public static final String WARNING_VERSION;
	public static final String WARNING_COUNT;
	public static final String WARNING_NUMBER;
	public static final String EVALUATION_VERSION;
	public static final String EVALUATION_LICENSE_DIRECTORY_PROPERTY;
	@Deprecated
	public static final String ENABLE_QUOTA_SERVER;
	public static final String WARNING_EVALUATION;
	public static final String GENERAL_WARNING_EVALUATION;
	private static boolean _ck;
	private static final long[] KEY_SIG_CONST;
	private static final String PUB_STORE;
	private static final String SUBJECT;
	private static final String KEY_NODE;
	private static final String ALIAS;
	private static final String STORE_PASS;
	private static final String EVAL_PUB_STORE;
	private static volatile long _uptime;
	private static final String SCHEDULE_DISABLED;
	private static final String UPTIME_INFO;
	private static final String UPTIME_EXP;
	private static final String ZK_BINARY_WARNING;
	private static final String ZK_ERROR_REPROT_URL;
	private static final String UTIL_IMPL;
	private static KeyStoreParam _keystoreParam;
	private static CipherParam _cipherParam;
	private static LicenseParam _licenseParam;
	private static RuntimeLicenseManager _licManager;
	private static KeyStoreParam _evalKeystoreParam;
	private static LicenseParam _evalLicenseParam;
	private static RuntimeLicenseManager _evalLicManager;
	private static final String RT_CL;
	static final String RT_PREFS;
	private static final String RT_TIMESTAMP;
	private static final Locator LOCATOR;
	private static final String _version;
	private static final byte NORMAL_MODE = 0;
	@Deprecated
	private static final byte SPECIAL_MODE = 1;
	private static final byte EVAL_MODE = 2;
	private static byte MODE;

	public static final Object init(Object object) {
		return Runtime.init(object, null);
	}

	public static final Object init(Object object, Object object2) {
		Object execution = null;
		Object object3;
		if (!_ck && WebApps.getCurrent() != null) {
			Runtime.error(ZK_BINARY_WARNING);
			if (_licManager != null) {
				_licManager.stopScheduler();
			}
			System.exit(-1);
		}
		if (_uptime > 0L && _uptime < System.currentTimeMillis()) {
			object3 = WebApps.getFeature((String) "ee") ? "EE" : "PE";
			object3 = UPTIME_EXP.replaceAll("\\{0\\}", (String) object3);
			Runtime.error((String) object3);
			execution = Executions.getCurrent();
			if (execution != null) {
				((Execution)execution).addAuResponse(UPTIME_EXP,
						(AuResponse) new AuAlert((String) object3, "ZK Eval Notice", "z-msgbox z-uptime"));
			}
		}
		if (!"error".equals(object2) && MODE != 0 && (object3 = Executions.getCurrent()) != null) {
			try {
				if (!(MODE != 2 || !(object instanceof AuService)
						|| object2 != null && ("onTimer".equals(object2) || "onRender".equals(object2)
								|| "onDataLoading".equals(object2) || "onScrollPos".equals(object2)
								|| "onTopPad".equals(object2) || "onAnchorPos".equals(object2)))) {
					RuntimeUtil.getInstance().updatePrefs();
				}
				if (((Execution) object3).getAttribute(RT_CL) == null) {
					 ((Execution) object3).setAttribute(RT_CL, Boolean.TRUE);
					execution = WebApps.getCurrent();
					if (((WebApp)execution).hasAttribute(RT_PREFS)) {
						Object l;
						boolean bl = false;
						if (((WebApp)execution).hasAttribute(RT_TIMESTAMP)) {
							l = (Long) ((WebApp)execution).getAttribute(RT_TIMESTAMP);
							boolean bl2 = l != null ? ((Long)l) > System.currentTimeMillis() : (bl = false);
						}
						if (!bl) {
							l = (JSONObject) JSONValue.parse((String) Base64Coder
									.decodeString((String) ((String) ((WebApp)execution).getAttribute(RT_PREFS))));
							String string = (String)((JSONObject) l).get((Object) "msg");
							if (string != null) {
								Runtime.error(string);
							}
							if (((Execution) object3).isAsyncUpdate(null)) {
								String string2;
								Preferences preferences;
								if (MODE == 2 && ((JSONObject) l).containsKey((Object) "script")
										&& ((string2 = (preferences = Preferences.userNodeForPackage(Runtime.class))
												.get("d.s.g", null)) == null
												|| !Runtime.isInSameDay(JSONs.j2d((String) string2), new Date()))) {
									Clients.evalJavaScript((String) ((String) ((JSONObject) l).get((Object) "script")));
									preferences.put("d.s.g", JSONs.d2j((Date) new Date()));
								}
								((WebApp)execution).setAttribute(RT_TIMESTAMP, (Object) (System.currentTimeMillis() + 600000L));
							}
						}
					}
				}
			} catch (Throwable throwable) {
				Runtime.sendError(throwable);
			}
		}
		return new RtInfo() {

			public void verify(Execution execution) {
			}

			public void verify(Session session) {
			}
		};
	}

	public static boolean isInSameDay(Date date, Date date2) {
		Calendar calendar = Calendar.getInstance();
		Calendar calendar2 = Calendar.getInstance();
		calendar.setTime(date);
		calendar2.setTime(date2);
		return calendar.get(1) == calendar2.get(1) && calendar.get(6) == calendar2.get(6);
	}

	private static final void error(String string) {
		Logger logger = LoggerFactory.getLogger((String) "global");
		if (logger.isErrorEnabled()) {
			logger.error(string);
		} else {
			System.err.println(string);
		}
	}

	private static final String read(String string) {
		try {
			InputStream inputStream = Runtime.class.getResourceAsStream("/metainfo/zk/" + string);
			if (inputStream != null) {
				return new String(Files.readAll((InputStream) inputStream));
			}
		} catch (Throwable throwable) {
			// empty catch block
		}
		return null;
	}

	public static final boolean init(WebApp webApp, boolean bl) {
		boolean bl2 = true;
		if (bl && !_ck) {
			URL uRL;
			_ck = true;
			bl2 = WebApps.getFeature((String) "ee") && "ZK EE".equals(Runtime.read("ee"))
					|| "ZK PE".equals(Runtime.read("pe"));
			String string = Library.getProperty((String) LICENSE_DIRECTORY_PROPERTY);
			boolean bl3 = false;
			URL uRL2 = uRL = string != null
					? Runtime.getURLFromString(string)
					: LOCATOR.getResource("/metainfo/zk/license/");
			if (uRL != null) {
				bl3 = _licManager.install(uRL);
			}
			if (bl2) {
				webApp = null;
			}
			if (bl3) {
				_licManager.setWapp(webApp);
				if (!"true".equals(Library.getProperty((String) SCHEDULE_DISABLED, (String) "false"))) {
					_licManager.startScheduler();
				}
				return true;
			}
			if (webApp != null) {
				MODE = (byte) 2;
				webApp.setAttribute("org.zkoss.zk.ui.notice", (Object) " Evaluation Only");
				Runtime.enableUptimeLimit();
				Runtime.runQuotaServer();
			}
		}
		return bl2;
	}

	static URL getURLFromString(String string) {
		try {
			return new File(string).toURI().toURL();
		} catch (IOException iOException) {
			Runtime.error("getURLFromString: " + iOException.getMessage());
			return null;
		}
	}

	static final void sendError(Throwable throwable) {
		StringWriter stringWriter = new StringWriter();
		PrintWriter printWriter = new PrintWriter(stringWriter);
		throwable.printStackTrace(printWriter);
		Runtime.sendError(stringWriter.toString());
	}

	static final void sendError(String string) {
		block6 : {
			String string2 = RuntimeUtil.getInstance().getUFlag();
			if (string2 != null) {
				string = System.getProperty("os.name") + " - " + System.getProperty("os.version") + ':' + _version + ":"
						+ string2 + "\n" + string;
				if (string.length() > 600) {
					string = string.substring(0, 600);
				}
				BufferedReader bufferedReader = null;
				try {
					URLConnection uRLConnection = new URL(ZK_ERROR_REPROT_URL + URLEncoder.encode(string, "UTF-8"))
							.openConnection();
					bufferedReader = new BufferedReader(new InputStreamReader(uRLConnection.getInputStream()));
					bufferedReader.close();
				} catch (Throwable throwable) {
					if (bufferedReader == null)
						break block6;
					try {
						bufferedReader.close();
					} catch (IOException iOException) {
						// empty catch block
					}
				}
			}
		}
	}

	public static final void enableUptimeLimit() {
		_uptime = Long.MAX_VALUE;
	}

	public static final boolean isQuotaServerEnabled() {
		return RuntimeUtil.getInstance().isEnabled();
	}

	public static final void runQuotaServer() {
		RuntimeUtil.getInstance().run((RuntimeLicense) _evalLicManager);
	}

	static {
		String string;
		COMPANY_NAME = new ObfuscatedString(
				new long[]{-7340139527016707886L, -5203243759892677212L, 8714822623115369524L}).toString();
		COMPANY_ADDRESS = new ObfuscatedString(
				new long[]{-8691848786421899489L, -4370996558863340632L, -2709933490946981238L}).toString();
		COMPANY_ZIPCODE = new ObfuscatedString(
				new long[]{-4934501068656857753L, -6913828373145765012L, 1063193634233753528L}).toString();
		COUNTRY = new ObfuscatedString(new long[]{-4504969373906269801L, 5248137891553083335L}).toString();
		PROJECT_NAME = new ObfuscatedString(new long[]{830623621279886032L, 459360910074040759L, 4178520520701877821L})
				.toString();
		PRODUCT_NAME = new ObfuscatedString(
				new long[]{5504882338648710617L, -4761731334749763195L, 996375918662338065L}).toString();
		PACKAGE = new ObfuscatedString(new long[]{-8439029564924938530L, -4878278112849633009L}).toString();
		VERSION = new ObfuscatedString(new long[]{-4847689528984584834L, 2493253216426408014L}).toString();
		ISSUE_DATE = new ObfuscatedString(
				new long[]{-4228764154858292882L, -6898004159031332466L, 6328666951570048917L}).toString();
		EXPIRY_DATE = new ObfuscatedString(
				new long[]{-7233890858958970371L, -3423973165832030856L, -5810612950970282077L}).toString();
		TERM = new ObfuscatedString(new long[]{-6725301182108235475L, -8691110408124621856L}).toString();
		VERIFICATION_NUMBER = new ObfuscatedString(
				new long[]{3823288740853721680L, -6436340937747658512L, 891079956768101415L, 751513662528431611L})
						.toString();
		INFORMATION = new ObfuscatedString(new long[]{378870925371295609L, 1418863983102047429L, -5017007170548372422L})
				.toString();
		KEY_SIGNATURE = new ObfuscatedString(
				new long[]{-2573177027008659676L, 5066716785755217927L, 5769746383701090690L}).toString();
		CHECK_PERIOD = new ObfuscatedString(
				new long[]{-2439022525501632135L, 6139476070014855270L, -297657911147084449L}).toString();
		LICENSE_DIRECTORY_PROPERTY = new ObfuscatedString(new long[]{6388899238000244134L, -5180024805664342415L,
				-367736596541156579L, -5744200612985226406L, -3419809629710828214L, -3830138366774401580L}).toString();
		LICENSE_VERSION = new ObfuscatedString(
				new long[]{-7080462743270045357L, 6928867389785115158L, -154565539896996742L}).toString();
		WARNING_EXPIRY = new ObfuscatedString(
				new long[]{-2088056424898980973L, -3616911578495445651L, -8353968737700076168L}).toString();
		WARNING_PACKAGE = new ObfuscatedString(
				new long[]{7436618834759965309L, 8220698497085578148L, -6394078374620879850L}).toString();
		WARNING_VERSION = new ObfuscatedString(
				new long[]{7417971821667979026L, 7464186339852802771L, 7986314911006223431L}).toString();
		WARNING_COUNT = new ObfuscatedString(
				new long[]{-1510608780643214737L, 6313704540210276937L, 4115504365890483558L}).toString();
		WARNING_NUMBER = new ObfuscatedString(
				new long[]{8367990676393660796L, -7163797910637480555L, -8349581027556623805L}).toString();
		EVALUATION_VERSION = new ObfuscatedString(
				new long[]{-8254333082681995594L, 2133816849348739600L, 569888272338784702L, 2363756875261694331L})
						.toString();
		EVALUATION_LICENSE_DIRECTORY_PROPERTY = new ObfuscatedString(
				new long[]{5224700506273109669L, -4821082329883784793L, 4483789110796661303L, -4130582028120586740L,
						6738877940215938826L, 5062964898350986461L}).toString();
		ENABLE_QUOTA_SERVER = new ObfuscatedString(
				new long[]{-8625977702787672529L, 512796179395572189L, -315657241020468189L, -5391257847076299574L})
						.toString();
		WARNING_EVALUATION = new ObfuscatedString(new long[]{-2937671592507492851L, -3102268496324167801L,
				5919586344461932935L, 5212380637393923732L, -9183933033647938375L, -5639401357834602762L,
				1900792064421269769L, 9104764753224514299L, -366291431641907250L, -4981951446456859208L,
				4621325894282509391L, 4597809274251359252L, 320404476883389261L, 4834059445850194399L,
				8189661530836744639L, 3321360458823117467L, -5649982894045768422L, 6824790053669282079L,
				2913592521239689585L, 792727853396105105L, -5806058457130989498L, -9213406388190282821L,
				6249188679012732153L, -5765366963941016085L, 6083980417978954274L, 5349913069684837185L,
				-8468607902170715087L, -231630823878680548L, -7028398969894246595L, -4717713309948778529L,
				-6063904821125494506L, -3266075547796484583L, -3094008223659612898L, 5979310533272008024L,
				-9081613057476004509L, 2696821719504131438L, 8070012780166355462L, -7898308780045792598L,
				7667082605530285313L, -8372608728474611036L, -4196725015623600895L}).toString();
		GENERAL_WARNING_EVALUATION = new ObfuscatedString(new long[]{-7957057441565557468L, -1288318705243094125L,
				3092143614677261383L, 4031913934475315515L, -7804868226880435395L, -3448853636653319651L,
				1871346180585307648L, 223053515308615678L, 6858453687253581000L, 7704682924214585077L,
				-222074611741028696L, 281034272861575975L, 6976786445615838851L, 5745024938444840495L,
				4632538930761627869L, -5022344712445976501L, -3538310803483450462L, 5352391551123025186L,
				3821965514548615447L, 6026980799427506308L, 6766928629646280530L, -7821411397181956273L,
				2485472262721140470L, 1450406675502229225L, 2050464362686498863L, -8719761546234154793L,
				7306874635934848121L, -5184754514579368811L, -3042267309723482540L, -8782547652253589093L,
				5664550114326321064L, 5212193069627527939L, 2637516048225976497L, 946618852495413183L,
				564044486294786844L, 2638013397330933025L, -4953561382259125827L, -6111916807271356845L,
				93293787242812403L, 2937633543045520084L, -1618454152578369380L}).toString();
		KEY_SIG_CONST = new long[]{-2094697673918916784L, 9043705620287016241L, 4353964247040842695L};
		PUB_STORE = new ObfuscatedString(
				new long[]{5347140503694695672L, -2050078472222786559L, -1090939667477166182L, -1614887116429751062L})
						.toString();
		SUBJECT = new ObfuscatedString(new long[]{-1712853941495412010L, 7139314103872750678L, 1708647545312167373L})
				.toString();
		KEY_NODE = new ObfuscatedString(new long[]{-1178633844166344050L, 942064108562773571L, 5467556404179583502L})
				.toString();
		ALIAS = new ObfuscatedString(new long[]{6186746913238977211L, 1118071448919910242L, 3268266759092267502L,
				8415914926935764836L, 8050900615525030165L}).toString();
		STORE_PASS = new ObfuscatedString(new long[]{8215178821005386738L, 3606545547696508380L, 8105569695056259631L,
				8978067179702718063L, 8980164237678180832L}).toString();
		EVAL_PUB_STORE = new ObfuscatedString(
				new long[]{4483574995589002284L, -1877769693911683148L, 6537753203970748177L, 7870286962621092528L})
						.toString();
		_uptime = -1L;
		SCHEDULE_DISABLED = new ObfuscatedString(new long[]{5671306788677542365L, -5885525372458450988L,
				-4407396626133081282L, -1537455835363693899L, -6031102665156057524L, -1478170211770868218L}).toString();
		UPTIME_INFO = new ObfuscatedString(
				new long[]{-140629977948033438L, 8411039821423448906L, -3474650949739896324L, 2772224587032503194L})
						.toString();
		UPTIME_EXP = new ObfuscatedString(
				new long[]{3013278860660930162L, 4301471526854951301L, -6747772497216156675L, 5645720567963480327L,
						6758055736309218282L, -1654731230014362443L, 274486467337341086L, 7040862789364547387L,
						7601088371900655428L, -2636893431972966937L, -952327415151053477L, 1248377226099978011L,
						-524117267375531873L, -2053861175643715280L, -740110635865205725L, -2536839436544335127L,
						6086939566297226655L, -5237082058524871272L, 1303639338833085497L, -6089458893752999207L,
						-3095527103706203994L, 3892762200748726282L, -9216442073544683168L, 2684244160057465715L,
						-2757286429096684263L, 3994511167925782802L, -6001272987401509012L, -8806371164062622569L,
						-8363562597909226838L, -6927551160336525363L, -4211670988672996905L, 5763267769455554081L,
						-117125249251658674L, 3683653926807866520L, -7901206021548053328L, 6018772853677880081L,
						-1465864270908134531L, -6931709528463852872L, 4789754509887854166L, 890869380472621700L,
						-4164923529230262421L, 2107536733021848521L, 1569761813715635289L, -4115748546260801472L})
								.toString();
		ZK_BINARY_WARNING = new ObfuscatedString(
				new long[]{527369764998585600L, 7829194491028862714L, -6992576942654891347L, -4074685035432387735L,
						-3751540757138090372L, -9013533065517180066L, 4451724886330983389L, -6392993511565310850L,
						-7319579990726818873L, -4449798807160623343L, -4453665840857292943L, -217880807298036412L,
						-5209034633849973134L, 2411790124719455892L, -1139725803346975429L, -5126415854884727677L})
								.toString();
		ZK_ERROR_REPROT_URL = new ObfuscatedString(new long[]{6194666952753217808L, 3823226537087564944L,
				-4283590027965885865L, 6437272674790304175L, 4130722627614603628L, 4299874090528894925L}).toString();
		UTIL_IMPL = new ObfuscatedString(new long[]{-9119357793812075433L, -281094700249698240L, 5406917946962033774L,
				-7188106790788417186L, -7484080138269982934L, -5119145590716415229L}).toString();
		_keystoreParam = new KeyStoreParam() {

			public InputStream getStream() throws IOException {
				InputStream inputStream = Classes.getContextClassLoader(Runtime.class).getResourceAsStream(PUB_STORE);
				if (inputStream == null) {
					throw new FileNotFoundException(PUB_STORE);
				}
				return inputStream;
			}

			public String getAlias() {
				return ALIAS;
			}

			public String getStorePwd() {
				return STORE_PASS;
			}

			public String getKeyPwd() {
				return null;
			}
		};
		_cipherParam = new CipherParam() {

			public String getKeyPwd() {
				return new ObfuscatedString(new long[]{-9017617134232705315L, -3067316756544620689L,
						-7174741455541659722L, 9223059116147577819L, -7389013047307896124L}).toString();
			}
		};
		_licenseParam = new LicenseParam() {

			public String getSubject() {
				return SUBJECT;
			}

			public Preferences getPreferences() {
				return null;
			}

			public KeyStoreParam getKeyStoreParam() {
				return _keystoreParam;
			}

			public CipherParam getCipherParam() {
				return _cipherParam;
			}
		};
		_licManager = RuntimeLicenseManager.getInstance((LicenseParam) _licenseParam);
		_evalKeystoreParam = new KeyStoreParam() {

			public InputStream getStream() throws IOException {
				InputStream inputStream = Classes.getContextClassLoader(Runtime.class)
						.getResourceAsStream(EVAL_PUB_STORE);
				if (inputStream == null) {
					throw new FileNotFoundException(EVAL_PUB_STORE);
				}
				return inputStream;
			}

			public String getAlias() {
				return ALIAS;
			}

			public String getStorePwd() {
				return STORE_PASS;
			}

			public String getKeyPwd() {
				return null;
			}
		};
		_evalLicenseParam = new EvalLicenseParam() {

			public String getSubject() {
				return SUBJECT;
			}

			public Preferences getPreferences() {
				return null;
			}

			public KeyStoreParam getKeyStoreParam() {
				return _evalKeystoreParam;
			}

			public CipherParam getCipherParam() {
				return _cipherParam;
			}
		};
		_evalLicManager = RuntimeLicenseManager.getInstance((LicenseParam) _evalLicenseParam);
		RT_CL = new ObfuscatedString(
				new long[]{5369801822528592945L, 4131644222152454727L, -8568743324321778912L, 2410573768741921741L})
						.toString();
		RT_PREFS = new ObfuscatedString(
				new long[]{-565558667013027914L, 2706949423404623976L, -1185472294175643547L, 2700952870953613515L})
						.toString();
		RT_TIMESTAMP = new ObfuscatedString(new long[]{-1400397395496866872L, -6076048055011238918L,
				2610382205060226760L, 1861152423740986487L, 3180211639163912568L}).toString();
		LOCATOR = Locators.getDefault();
		try {
			string = Integer.toString(Integer.parseInt("8.5.2.1".replace(".", "")), 36) + ":"
					+ Integer.toString(Integer.parseInt(AbstractWebApp.getBuildStamp().replace("\\.", "")), 36);
		} catch (Exception exception) {
			string = "8.5.2.1:" + AbstractWebApp.getBuildStamp();
		}
		_version = string;
		try {
			Classes.forNameByThread((String) UTIL_IMPL);
		} catch (Throwable throwable) {
			// empty catch block
		}
		MODE = 0;
	}

	static interface EvalLicenseParam extends LicenseParam {
	}

}