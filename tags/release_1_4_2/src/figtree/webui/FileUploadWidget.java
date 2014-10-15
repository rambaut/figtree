package figtree.webui;

import eu.webtoolkit.jwt.Side;
import eu.webtoolkit.jwt.Signal;
import eu.webtoolkit.jwt.Signal1;
import eu.webtoolkit.jwt.WApplication;
import eu.webtoolkit.jwt.WContainerWidget;
import eu.webtoolkit.jwt.WFileUpload;
import eu.webtoolkit.jwt.WLabel;
import eu.webtoolkit.jwt.WText;

public class FileUploadWidget extends WContainerWidget {
	
	private WFileUpload fileUpload;
	private Signal1<String> fileUploaded = new Signal1<String>();
	private WLabel label;
	private WText currentTree;

	public FileUploadWidget() {
		currentTree = new WText(this);
		currentTree.setStyleClass("filename");
		currentTree.setInline(false);
		currentTree.setFloatSide(Side.Right);
		label = new WLabel("Upload tree: ", this);
		createUpload();
	}

	private void createUpload() {
		fileUpload = new WFileUpload(this);
		fileUpload.changed().addListener(this, new Signal.Listener() {
			@Override
			public void trigger() {
				fileUpload.upload();
			} });

		fileUpload.uploaded().addListener(this, new Signal.Listener() {
			@Override
			public void trigger() {
				handleUpload();
			}
		});
	}

	private void handleUpload() {
		WApplication.getInstance().setTitle("FigTree: " + fileUpload.getClientFileName());
		currentTree.setText("Read file: <i>" + fileUpload.getClientFileName() + "</i>");
		fileUploaded.trigger(fileUpload.getSpoolFileName());

		fileUpload.remove();
		createUpload();
	}

	public Signal1<String> fileUploaded() {
		return fileUploaded;
	}
}
