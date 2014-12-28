 
package edu.usc.cssl.nlputils.plugins.supremeCrawler.parts;

import java.io.IOException;

import javax.inject.Inject;
import javax.annotation.PostConstruct;

import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;

import edu.usc.cssl.nlputils.plugins.supremeCrawler.process.SupremeCrawler;

public class SupremeGUI {
	private Text txtOutput;
	@Inject
	public SupremeGUI() {
		
	}
	
	@PostConstruct
	public void postConstruct(Composite parent) {
		final Shell shell = parent.getShell();
		parent.setLayout(new GridLayout(15, false));
		
		Label lblSortBy = new Label(parent, SWT.NONE);
		lblSortBy.setText("Filter Type");
		new Label(parent, SWT.NONE);;
		
		Button btnCases = new Button(parent, SWT.RADIO);
		btnCases.setSelection(true);
		btnCases.setText("Term");
		new Label(parent, SWT.NONE);
		
		Button btnIssues = new Button(parent, SWT.RADIO);
		btnIssues.setText("Issues");
		new Label(parent, SWT.NONE);
		new Label(parent, SWT.NONE);
		new Label(parent, SWT.NONE);
		new Label(parent, SWT.NONE);
		new Label(parent, SWT.NONE);
		new Label(parent, SWT.NONE);
		new Label(parent, SWT.NONE);
		new Label(parent, SWT.NONE);
		new Label(parent, SWT.NONE);
		new Label(parent, SWT.NONE);
		
		Label lblFilter = new Label(parent, SWT.NONE);
		lblFilter.setText("Filter Range");
		new Label(parent, SWT.NONE);
		
		final Combo combo=new Combo(parent, SWT.NONE);
		GridData gd_combo = new GridData(SWT.FILL, SWT.CENTER, true, false, 13, 1);
		gd_combo.widthHint = 177;
		combo.setLayoutData(gd_combo);
		appendLog("Loading Filters...");
		combo.setItems(SupremeCrawler.filters("cases"));
		combo.select(0);
		
		Label lblOutputDirectory = new Label(parent, SWT.NONE);
		lblOutputDirectory.setText("Output Directory");
		new Label(parent, SWT.NONE);
		
		txtOutput = new Text(parent, SWT.BORDER);
		GridData gd_txtOutput = new GridData(SWT.FILL, SWT.CENTER, false, false, 9, 1);
		gd_txtOutput.widthHint = 260;
		txtOutput.setLayoutData(gd_txtOutput);
		
		Button button = new Button(parent, SWT.NONE);
		button.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseUp(MouseEvent e) {
				DirectoryDialog od = new DirectoryDialog(shell);
				od.open();
				String oDirectory = od.getFilterPath();
				txtOutput.setText(oDirectory);
			}
		});
		button.setText("...");
		new Label(parent, SWT.NONE);
		new Label(parent, SWT.NONE);
		new Label(parent, SWT.NONE);
		
		Button btnDownloadAudio = new Button(parent, SWT.CHECK);
		btnDownloadAudio.setSelection(true);
		btnDownloadAudio.setText("Download Audio");
		new Label(parent, SWT.NONE);
		
		Button btnTruncate = new Button(parent, SWT.CHECK);
		btnTruncate.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 3, 1));
		btnTruncate.setText("Truncate MP3 (1MB)");
		new Label(parent, SWT.NONE);
		new Label(parent, SWT.NONE);
		new Label(parent, SWT.NONE);
		new Label(parent, SWT.NONE);
		new Label(parent, SWT.NONE);
		new Label(parent, SWT.NONE);
		new Label(parent, SWT.NONE);
		new Label(parent, SWT.NONE);
		new Label(parent, SWT.NONE);
		new Label(parent, SWT.NONE);
		
		Button btnCrawl = new Button(parent, SWT.NONE);
		btnCrawl.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseUp(MouseEvent e) {
				appendLog("Crawling...");
				String f = combo.getText();
				if(f.equals("All")){
					if(btnCases.getSelection())
						f = "/cases";
					else
						f = "/issues";
				}
				long startTime = System.currentTimeMillis();
				SupremeCrawler sc = new SupremeCrawler(combo.getText(), txtOutput.getText(), btnTruncate.getSelection(), btnDownloadAudio.getSelection());
				IEclipseContext iEclipseContext = context;
				ContextInjectionFactory.inject(sc,iEclipseContext);
				try {
					sc.looper();
				} catch (IOException e1) {
					e1.printStackTrace();
				}
				appendLog("Crawling completed in "+(System.currentTimeMillis()-startTime)/(float)1000+" seconds");
				appendLog("DONE\n");
			}
		});
		btnCrawl.setText("Crawl");
		new Label(parent, SWT.NONE);
		new Label(parent, SWT.NONE);
		new Label(parent, SWT.NONE);
		new Label(parent, SWT.NONE);
		new Label(parent, SWT.NONE);
		new Label(parent, SWT.NONE);
		new Label(parent, SWT.NONE);
		new Label(parent, SWT.NONE);
		new Label(parent, SWT.NONE);
		new Label(parent, SWT.NONE);
		new Label(parent, SWT.NONE);
		new Label(parent, SWT.NONE);
		new Label(parent, SWT.NONE);
		new Label(parent, SWT.NONE);
		
		btnCases.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				appendLog("Loading Filters...");
				System.out.println("Term is "+btnCases.getSelection());
				if(btnCases.getSelection())
					combo.setItems(SupremeCrawler.filters("cases"));
				else
					combo.setItems(SupremeCrawler.filters("issues"));
				combo.select(0);
			}
		});
		
		
	}
	
	@Inject IEclipseContext context;
	private void appendLog(String message){
		IEclipseContext parent = context.getParent();
		String currentMessage = (String) parent.get("consoleMessage"); 
		if (currentMessage==null)
			parent.set("consoleMessage", message);
		else {
			if (currentMessage.equals(message)) {
				// Set the param to null before writing the message if it is the same as the previous message. 
				// Else, the change handler will not be called.
				parent.set("consoleMessage", null);
				parent.set("consoleMessage", message);
			}
			else
				parent.set("consoleMessage", message);
		}
	}
	
	
}