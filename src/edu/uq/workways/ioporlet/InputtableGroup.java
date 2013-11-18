package edu.uq.workways.ioporlet;

import java.util.Set;

public class InputtableGroup extends DisplayObject {

	@Override
	public int getNumberOfSeries() {
		return 0;
	}

	@Override
	public void addData(String data, String serieId, boolean update)
			throws UpperLimitNumberOfSeriesException, InvalidDataException {
	}

	@Override
	public Set<String> getDataSeriesIds() {
		return null;
	}

	@Override
	public void update() throws InvalidDataException {

	}

}
