package org.simpl4;

import org.junit.runners.model.InitializationError;
import org.junit.runners.BlockJUnit4ClassRunner;

public class OrientdbTestRunner extends BlockJUnit4ClassRunner {
	public OrientdbTestRunner(Class<?> classToRun) throws InitializationError {
		super(classToRun);
	}

	@Override
	public Object createTest() throws Exception{
		return super.createTest();
	}
}

