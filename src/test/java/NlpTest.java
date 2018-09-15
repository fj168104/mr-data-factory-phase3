

import com.mr.RootApplication;
import com.mr.framework.core.lang.Console;
import com.mr.modules.api.site.instance.colligationsite.mofcomsite.NLP_Ner_API;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * Created by feng on 18-9-2
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = RootApplication.class)
@EnableAutoConfiguration
public class NlpTest{

	@Autowired
	private NLP_Ner_API api;

	@Test
	public void test1(){
		Console.log(api.getUrl());
	}
}
