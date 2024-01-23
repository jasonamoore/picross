package resource.image;

public class RasterSheet {

	private Raster[] rasters;
	
	public Raster getRasterByIndex(int index) {
		return rasters[index];
	}

}
