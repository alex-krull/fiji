package blobTracking;

import java.util.Properties;

import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.NumericType;
import net.imglib2.type.numeric.RealType;
import frameWork.MovieChannel;
import frameWork.Policy;
import frameWork.Session;
import frameWork.gui.controlWindow.BlobOptionDialog;
import frameWork.gui.controlWindow.MScaleOption;
import frameWork.gui.controlWindow.SessionOptionsDialog;

public class BlobSession  <IT extends NumericType<IT> & NativeType<IT> & RealType<IT>> extends Session<Blob,IT>{

	private double defaultSigma=1;
	private double defaultMaxSigma=2;
	private double defaultMinSigma=0.5;
	private boolean autoSigma=false;
	private double defaultSigmaZ=2;
	private double defaultMaxSigmaZ=2;
	private double defaultMinSigmaZ=0.5;
	private boolean autoSigmaZ=false;
	private double downscaleFactor = .5;
	private double mscaleSigma = 1;
	private int mscaleIterations = 5;
	
	private double changeFactorSigma=10;
	private double changeFactorPK=1;
	
	public void setChangeFactorSigma(double changeFactorSigma) {
		this.changeFactorSigma = changeFactorSigma;
	}


	public void setChangeFactorPK(double changeFactorPK) {
		this.changeFactorPK = changeFactorPK;
	}


	
	
	public double getChangeFactorSigma() {
		return changeFactorSigma;
	}


	public double getChangeFactorPK() {
		return changeFactorPK;
	}


	public BlobSession(int newID, Policy<Blob, IT> pol, MovieChannel<IT> mc) {
		super(newID, pol, mc);
	}
	
	
	public double getDefaultSigma() {
		return defaultSigma;
	}

	public void setDefaultSigma(double defaultSigma) {
		this.defaultSigma = defaultSigma;
	}

	public double getDefaultMaxSigma() {
		return defaultMaxSigma;
	}

	public void setDefaultMaxSigma(double defaultMaxSigma) {
		this.defaultMaxSigma = defaultMaxSigma;
	}

	public double getDefaultMinSigma() {
		return defaultMinSigma;
	}

	public void setDefaultMinSigma(double defaultMinSigma) {
		this.defaultMinSigma = defaultMinSigma;
	}

	public boolean isAutoSigma() {
		return autoSigma;
	}

	public void setAutoSigma(boolean autoSigma) {
		this.autoSigma = autoSigma;
	}

	public double getDefaultSigmaZ() {
		return defaultSigmaZ;
	}

	public void setDefaultSigmaZ(double defaultSigmaZ) {
		this.defaultSigmaZ = defaultSigmaZ;
	}

	public double getDefaultMaxSigmaZ() {
		return defaultMaxSigmaZ;
	}

	public void setDefaultMaxSigmaZ(double defaultMaxSigmaZ) {
		this.defaultMaxSigmaZ = defaultMaxSigmaZ;
	}

	public double getDefaultMinSigmaZ() {
		return defaultMinSigmaZ;
	}

	public void setDefaultMinSigmaZ(double defaultMinSigmaZ) {
		this.defaultMinSigmaZ = defaultMinSigmaZ;
	}

	public boolean isAutoSigmaZ() {
		return autoSigmaZ;
	}

	public void setAutoSigmaZ(boolean autoSigmaZ) {
		this.autoSigmaZ = autoSigmaZ;
	}

	
	@Override
	public void showPropertiesDialog() {
		  new SessionOptionsDialog(this);
	}
	
	@Override
	public void showObjectPropertiesDialog(Blob b) {
		 new BlobOptionDialog(b,this.isVolune());
	}
	
	@Override
	public void showAlternatePropertiesDialog() {

		new MScaleOption(this);
	}
	
	


	public int getMscaleIterations() {
		return mscaleIterations;
	}


	public void setMscaleIterations(int mscaleIterations) {
		this.mscaleIterations = mscaleIterations;
	}


	public double getDownscaleFactor() {
		return downscaleFactor;
	}


	public void setDownscaleFactor(double downscaleFactor) {
		this.downscaleFactor = downscaleFactor;
	}


	public double getMscaleSigma() {
		return mscaleSigma;
	}


	public void setMscaleSigma(double mscaleSigma) {
		this.mscaleSigma = mscaleSigma;
	}


	@Override
	public void setProperties(Properties props){
		super.setProperties(props);
		String s;
		s= props.getProperty("mscaleIterations"); if(s!=null) this.mscaleIterations=Integer.valueOf(s);
		s= props.getProperty("mscaleSigma"); if(s!=null) this.mscaleSigma=Double.valueOf(s);
		s= props.getProperty("downscaleFactor"); if(s!=null) this.downscaleFactor=Double.valueOf(s);
		
		
	}
	
	@Override
	public Properties getProperties(){
		Properties props= super.getProperties();
		
		props.setProperty("mscaleIterations",String.valueOf(mscaleIterations));
		props.setProperty("mscaleSigma", String.valueOf(mscaleSigma));
		props.setProperty("downscaleFactor", String.valueOf(downscaleFactor));
		
	
		return props;
	}
	
	

}
