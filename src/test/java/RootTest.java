
import com.mr.RootApplication;
import com.mr.common.OCRUtil;
import com.mr.common.util.AIOCRUtil;
import com.mr.common.util.BaiduOCRUtil;
import com.mr.framework.core.lang.Console;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.File;


/**
 * Created by feng on 18-8-10
 */

@RunWith(SpringRunner.class)
@SpringBootTest(classes = RootApplication.class)
public class RootTest {

	@Autowired
	private OCRUtil ocrUtil;
	@Autowired
	private BaiduOCRUtil baiduOCRUtil;

	@Test
	public void testOcr(){
		String url = "http://zhanjiang.customs.gov.cn/zhanjiang_customs/534855/534876/534878/534880/1892731/2018061910313952865.pdf";
		String ss = AIOCRUtil.getTextFromImageUrl(url);
		Console.log(ss);
	}

	@Test
	public void testOcrFromPDF() throws Exception {
		String filePath = "/home/fengjiang/Downloads";
		String fileName = "14031A.jpg";

//		String text = extractWebDOCXLSData(filePath, fileName);
//		OcrUtils ocrUtils = new OcrUtils("/home/fengjiang/Downloads");
//		;
		Console.log(BaiduOCRUtil.getTextStrFromImageFile(filePath + File.separator + fileName));
//		Console.log(BaiduOCRUtil.getTextStrFromPDFFile(filePath, fileName));
	}

	@Test
	public void testOcrFromImg() throws Exception {
		String filePath = "/home/fengjiang/Downloads";
		String fileName = "2018081018062024316.tif";

//		String text = extractWebDOCXLSData(filePath, fileName);
//		OcrUtils ocrUtils = new OcrUtils("/home/fengjiang/Downloads");

//		Console.log(ocrUtils.getTextFromImg(fileName));
		Console.log(BaiduOCRUtil.getTextStrFromTIFFile(filePath, fileName));

//		BaiduOCRUtil.geta
	}

}
