package org.simpl4;

import org.junit.runners.model.InitializationError;

public class OrientdbTestRunner extends BlockJUnit4ClassRunner {
	public OrientdbTestRunner(Class<?> classToRun) throws InitializationError {
		super(classToRun);
	}

//	@Override
//	public Object createTest() {
//		return getInjector().getInstance(getTestClass().getJavaClass());
//	}

	@Override
	public Object createTest() {
		//Ensure that wicket tester and corresponding application started
		//getInjector().getInstance(WicketTester.class);
		return super.createTest();
	}

}

