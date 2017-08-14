package org.prototype.template;

import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

/**
 * 引擎服务实现
 * 
 * @author lj
 *
 */
@Service
@Slf4j
public class EngineServiceImpl implements EngineService {

	@Resource
	private ApplicationContext context;

	private Map<String, Engine> engines = null;

	/**
	 * 缓存模板引擎
	 */
	@PostConstruct
	void init() {
		if (engines != null) {
			return;
		}
		synchronized (this) {
			engines = new HashMap<>();
		}
		Map<String, Engine> map = context.getBeansOfType(Engine.class);
		if (map == null) {
			return;
		}
		for (Engine engine : map.values()) {
			engines.put(engine.getType(), engine);
		}
	}

	@Override
	public String render(String type, URL template,String encoding, Map<String, Object> properties) {
		try {
			String str = read(template.openStream(),encoding);
			return render(type, str, properties);
		} catch (IOException e) {
			throw new RuntimeException("IO error", e);
		}
	}

	@Override
	public String render(String type, String template, Map<String, Object> properties) {
		Engine e =engines.get(type);
		if (e == null) {
			throw new NullPointerException("Engine " + type + " is not found");
		}
		return e.render(template, properties);
	}

	@Override
	public String render(String type, File template,String encoding, Map<String, Object> properties) {
		try {
			String str = read(new FileInputStream(template),encoding);
			return render(type, str, properties);
		} catch (FileNotFoundException e) {
			throw new RuntimeException("IO error", e);
		}
	}

	/**
	 * 读取字符串
	 * @param is
	 * @return
	 */
	private String read(InputStream is,String encoding) {
		StringWriter writer=new StringWriter();
		Reader reader=null;		
		try{
			reader=new InputStreamReader(is,encoding);
			char[] chars=new char[1024];
			int k=0;
			while((k=reader.read(chars))!=-1){
				writer.write(chars,0,k);
			}
			return writer.toString();
		} catch (IOException e) {
			throw new RuntimeException("IO error", e);
		} finally {
			close(reader);
			close(writer);
		}
	}

	/**
	 * 关闭流
	 * @param is
	 */
	private void close(Closeable is) {
		if (is != null) {
			try {
				is.close();
			} catch (IOException e) {
				log.warn("Close io error", e);
			}
		}
	}

	@Override
	public boolean hasEngine(String type) {
		return engines.get(type)!=null;
	}

}
