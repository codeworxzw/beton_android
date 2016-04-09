package kz.bapps.e_concrete.signature;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import org.apache.xml.security.encryption.XMLCipherParameters;
import org.apache.xml.security.keys.KeyInfo;
import org.apache.xml.security.signature.XMLSignature;
import org.apache.xml.security.transforms.Transforms;
import org.apache.xml.security.utils.Constants;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xwalk.core.JavascriptInterface;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.StringWriter;
import java.nio.charset.Charset;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.Provider;
import java.security.Security;
import java.security.cert.X509Certificate;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Enumeration;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import kz.bapps.e_concrete.EConcrete;
import kz.gov.pki.kalkan.asn1.pkcs.PKCSObjectIdentifiers;
import kz.gov.pki.kalkan.jce.provider.KalkanProvider;
import kz.gov.pki.kalkan.xmldsig.KncaXS;


public class KalkanHelper {

    final private static String LOG_TAG = "KalkanHelper";

    private Context mContext;

    private Provider kalkanProvider;

    private OnKalkanHelperListener mListener;

    /** Instantiate the interface and set the context */
    public KalkanHelper(Context c) {
        mContext = c;
        if(c instanceof OnKalkanHelperListener) {
            mListener = (OnKalkanHelperListener) c;
        }

        kalkanProvider = new KalkanProvider();
        Security.addProvider(kalkanProvider);
        KncaXS.loadXMLSecurity();
    }

    public interface OnKalkanHelperListener {
        void onShowMessage(String message);
    }


    @JavascriptInterface
    public boolean fileExists() {
        SharedPreferences prefs = mContext.getSharedPreferences(EConcrete.appName, Context.MODE_PRIVATE);
        String pathEds = prefs.getString("edspath","");
        File file = new File(pathEds);

        if(!file.exists() && mListener != null) {
            mListener.onShowMessage("Выберите файл ЭЦП");
        }

        return file.exists();
    }

    private InputStream getEDSInputStream () {

        try {
            SharedPreferences prefs = mContext.getSharedPreferences(EConcrete.appName, Context.MODE_PRIVATE);
            String pathEds = prefs.getString("edspath","");
            return new FileInputStream(pathEds);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        return null;
    }


    @JavascriptInterface
    public String getSubjectDN (String pwd) {

        if(!fileExists()) {
            return null;
        }

        try {

            KeyStore ks = KeyStore.getInstance("PKCS12", kalkanProvider.getName());
            ks.load(getEDSInputStream(), pwd.toCharArray());
            Enumeration<String> als = ks.aliases();
            String al = null;
            while (als.hasMoreElements()) {
                al = als.nextElement();
            }

            X509Certificate x509Certificate = (X509Certificate) ks.getCertificate(al);

            String result = x509Certificate.getSubjectDN().toString();
            @SuppressLint("SimpleDateFormat") DateFormat df = new SimpleDateFormat("dd.MM.yyyy");

            result += ",beginDate=" + df.format(x509Certificate.getNotBefore());
            result += ",endDate=" + df.format(x509Certificate.getNotAfter());

            return result;

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    @JavascriptInterface
    public String doSignature(String xmlToSign, String pwd) {

        if(!fileExists()) {
            return null;
        }

        try {

            Log.d(LOG_TAG, xmlToSign);
            InputStream isXML = new ByteArrayInputStream(xmlToSign.getBytes(Charset.forName("UTF-8")));
            KeyStore ks = KeyStore.getInstance("PKCS12", kalkanProvider.getName());
            ks.load(getEDSInputStream(), pwd.toCharArray());
            Enumeration<String> als = ks.aliases();
            String al = null;
            while (als.hasMoreElements()) {
                al = als.nextElement();
            }

            /** Показываем подписываемый контент
             ---------------------------------------*/
//            ((TextView) findViewById(R.id.alias)).setText(al);
            PrivateKey key = (PrivateKey) ks.getKey(al, pwd.toCharArray());
            X509Certificate x509Certificate = (X509Certificate) ks.getCertificate(al);

            //подписываем XML

            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            dbf.setNamespaceAware(true);
            DocumentBuilder documentBuilder = dbf.newDocumentBuilder();
            Document doc = documentBuilder.parse(isXML);

            String signMethod;
            String digestMethod;

            String sigAlgOid = x509Certificate.getSigAlgOID();

            /** Показываем зашифрованную
            ---------------------------------------*/
//            ((TextView) findViewById(R.id.algorithm)).setText(sigAlgOid);
            if (sigAlgOid.equals(PKCSObjectIdentifiers.sha1WithRSAEncryption.getId())) {
                signMethod = Constants.MoreAlgorithmsSpecNS + "rsa-sha1";
                digestMethod = Constants.MoreAlgorithmsSpecNS + "sha1";
            } else if (sigAlgOid.equals(PKCSObjectIdentifiers.sha256WithRSAEncryption.getId())) {
                signMethod = Constants.MoreAlgorithmsSpecNS + "rsa-sha256";
                digestMethod = XMLCipherParameters.SHA256;
            } else {
                signMethod = Constants.MoreAlgorithmsSpecNS + "gost34310-gost34311";
                digestMethod = Constants.MoreAlgorithmsSpecNS + "gost34311";
            }

            XMLSignature xsig = new XMLSignature(doc, "", signMethod);

            if (doc.getFirstChild() != null) {
                doc.getFirstChild().appendChild(xsig.getElement());
                Transforms transforms = new Transforms(doc);
                transforms.addTransform(Transforms.TRANSFORM_ENVELOPED_SIGNATURE);
                transforms.addTransform(XMLCipherParameters.N14C_XML_CMMNTS);
                xsig.addDocument("", transforms, digestMethod);
                xsig.addKeyInfo(x509Certificate);
                xsig.sign(key);
                StringWriter os = new StringWriter();
                TransformerFactory tf = TransformerFactory.newInstance();
                Transformer trans = tf.newTransformer();
                trans.transform(new DOMSource(doc), new StreamResult(os));
                os.close();


                return os.toString();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return null;
    }
}
