package org.burningwave.common;

import org.junit.platform.runner.JUnitPlatform;
import org.junit.platform.suite.api.ExcludeTags;
import org.junit.platform.suite.api.SelectClasses;
import org.junit.runner.RunWith;

@SuppressWarnings("unused")
@RunWith(JUnitPlatform.class)
//@SelectPackages("org.burningwave.common")
@SelectClasses({

})
@ExcludeTags("Heavy")
public class AllExceptHeavyTestsSuite {

}