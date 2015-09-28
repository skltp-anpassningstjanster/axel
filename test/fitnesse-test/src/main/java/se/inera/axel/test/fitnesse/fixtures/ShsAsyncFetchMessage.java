package se.inera.axel.test.fitnesse.fixtures;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.io.FileUtils;

import se.inera.axel.shs.cmdline.ShsCmdline;
import se.inera.axel.shs.processor.ShsLabelMarshaller;
import se.inera.axel.shs.xml.label.Meta;
import se.inera.axel.shs.xml.label.ShsLabel;

public class ShsAsyncFetchMessage extends ShsBaseTest {

	static ShsLabelMarshaller shsLabelMarshaller = new ShsLabelMarshaller();

	private String txId;
	private String toAddress;
	private String productTypeId;
	private String meta;
	private String subject;
	private boolean fetched = false;
	private File inFile;
    private int maxWaitInSeconds = 0;

    public void setDeliveryServiceUrl(String deliveryServiceUrl) {
        System.setProperty("shsServerUrlDs", deliveryServiceUrl);
    }

	public boolean receivedFileIsCorrect() throws Throwable {
		fetchMessage();

		// Verify that the received file is identical to what was sent in before
		File outFile = new File("target/shscmdline/" + this.txId + "-0");
		boolean isEqual = FileUtils.contentEquals(this.inFile, outFile);

		return isEqual;
	}

	public String datapart() throws Throwable {
		fetchMessage();

		File outFile = new File("target/shscmdline/" + this.txId + "-0");

		return FileUtils.readFileToString(outFile);
	}

	private void fetchMessage() throws Throwable {
		if (fetched) {
			return;
		}

		List<String> args = new ArrayList<String>();
		args = addIfNotNull(args, SHS_FETCH);
		args = addIfNotNull(args, "-t", this.toAddress);
		args = addIfNotNull(args, "-i", this.txId);
		args = addIfNotNull(args, "-p", this.productTypeId);
		final String[] stringArray = args.toArray(new String[args.size()]);

        ShsLabel fetchedLabel = AsynchFetcher.fetch(maxWaitInSeconds, new AsynchFetcher.Fetcher<ShsLabel>() {
            @Override
            public ShsLabel fetch() throws Throwable {
                System.out.print(System.getProperty("line.separator") + "arguments: ");
                for (String param : stringArray) {
                    System.out.print(param + " ");
                }
                System.out.print(System.getProperty("line.separator"));

                try {
                    ShsCmdline.main(stringArray);
                } catch (Exception e) {
                    System.out.println(String.format("Failed to fetch message with exception %s. Returning null", e.getMessage()));
                    return null;
                }

                InputStream stream = new BufferedInputStream(new FileInputStream(
                        "target/shscmdline/" + txId + "-label"));
                ShsLabel label = shsLabelMarshaller.unmarshal(stream);

                return label;
            }
        });

        if (fetchedLabel == null) {
            throw new RuntimeException(String.format("Message could not be fetched with arguments %s", Arrays.toString(stringArray)));
        }

		// Retrieve meta data
		List<Meta> metaList = fetchedLabel.getMeta();

		if (metaList.size() > 0) {
			Meta item = metaList.get(0);
			String name = item.getName();
			String value = item.getValue();
			this.meta = name + "=" + value;
		}

		// Retrieve subject
		this.subject = fetchedLabel.getSubject();

		fetched = true;
	}

	public String getTxId() {
		return txId;
	}

	public void setTxId(String txId) {
		this.txId = txId;
	}

	public void setToAddress(String toAddress) {
		this.toAddress = toAddress;
	}

	public void setInputFile(String inputFile) {
        boolean isAbsolutePath = inputFile.contains("/") || inputFile.contains("\\");
		if (!isAbsolutePath) {
			URL fileUrl = ClassLoader.getSystemResource(inputFile);
			if (fileUrl == null) {
				throw new IllegalArgumentException("File with name "
						+ inputFile + " could not be found");
			}
			inputFile = fileUrl.getFile();
		}

		this.inFile = new File(inputFile);
		if (!this.inFile.exists()) {
			throw new IllegalArgumentException("File with name " + inputFile
					+ " could not be found");
		}
	}

	public void setProductTypeId(String productTypeId) {
		this.productTypeId = productTypeId;
	}

	public void setCharset(String charset) {
	}

	public String meta() {
		return this.meta;
	}

	public String subject() {
		return this.subject;
	}

    public void setMaxWaitInSeconds(int maxWaitInSeconds) {
        this.maxWaitInSeconds = maxWaitInSeconds;
    }
}
