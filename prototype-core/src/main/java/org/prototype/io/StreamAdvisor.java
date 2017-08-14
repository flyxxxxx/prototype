package org.prototype.io;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import org.prototype.core.Errors;
import org.prototype.core.MethodBuilder;
import org.prototype.core.MethodAdvisor;
import org.prototype.core.MethodChain;
import org.prototype.core.MethodFilter;

@Component
public class StreamAdvisor implements MethodAdvisor,ApplicationContextAware {

	private ApplicationContext applicationContext;
	
	private Map<Class<?>, Output<?>> outputs=new HashMap<>();
	private Map<Class<?>[], Translator<?,?>> translators=new HashMap<>();

	public StreamAdvisor() throws InstantiationException, IllegalAccessException{
		Class<?>[] types=new Class<?>[]{FileOutput.class,BytesOutput.class,StringOutput.class};
		for(Class<?> type:types){
			Output<?> output=(Output<?>) type.newInstance();
			outputs.put(output.getSupportedType(), output);
		}
		types=new Class<?>[]{StringToOutputTranslator.class,StringToWriterTranslator.class,BytesToOutputTranslator.class,BytesToWriterTranslator.class};
		for(Class<?> type:types){
			Translator<?,?> translator=(Translator<?,?>) type.newInstance();
			translators.put(new Class<?>[]{translator.getSourceType(),translator.getTargetType()}, translator);
		}
	}

	@Override
	public MethodFilter<?> matches(MethodBuilder builder, Errors errors) {
		Stream stream = builder.getAnnotation(Stream.class);
		if (stream == null) {
			return null;
		}
		return true?null:null;
	}

	//@Override
	public Object doAround(MethodChain chain, Object[] args) throws Exception {
		Stream stream = chain.getMethod().getAnnotation(Stream.class);
		Class<?> returnType = chain.getMethod().getReturnType();
		Object output = null, input = null;
		if (void.class.equals(returnType)) {
			int length = args.length - 1;
			output = args[length];
			Object[] array = new Object[length];
			System.arraycopy(args, 0, array, 0, length);
			input = getInput(array, stream);
		} else {
			output = createOutput(returnType,stream);
			input = getInput(args, stream);
		}
		transimitStream(input, output,stream);
		if (void.class.equals(returnType)) {
			return null;
		} else {
			return output(returnType,output);
		}
	}

	@SuppressWarnings("unchecked")
	private void transimitStream(Object input, Object output,Stream stream) throws IOException{
		for(Map.Entry<Class<?>[], Translator<?,?>> entry:translators.entrySet()){
			Class<?>[] key=entry.getKey();
			if(key[0].isInstance(input)&&key[1].isInstance(output)){
				((Translator<Object,Object>)entry.getValue()).transfer(input, output, stream);
				return;
			}
		}
		throw new UnsupportedOperationException();
	}

	private Object getInput(Object[] args, Stream stream) {
		if (args.length == 1) {
			Class<?> clazz=args[0].getClass();
			if (clazz.isArray()&&!clazz.getComponentType().isPrimitive()) {
				return new InputStreamImpl((Object[]) args[0]);
			}
			return args[0];
		} else {
		}
		return new InputStreamImpl(args);
	}

	/**
	 * 创建输出
	 * 
	 * @param returnType
	 * @return
	 */
	private Object createOutput(Class<?> returnType,Stream stream) throws IOException{
		for(Map.Entry<Class<?>, Output<?>> entry:outputs.entrySet()){
			if(entry.getKey().isAssignableFrom(returnType)){
				return entry.getValue().create(stream);
			}
		}
		throw new UnsupportedOperationException();
	}

	/**
	 * 返回输出
	 * 
	 * @param output
	 * @return
	 */
	@SuppressWarnings("unchecked")
	private Object output(Class<?> returnType,Object output) throws IOException {
		for(Map.Entry<Class<?>, Output<?>> entry:outputs.entrySet()){
			if(entry.getKey().isAssignableFrom(returnType)){
				Output<Object> put=(Output<Object>) entry.getValue();
				return put.output(output);
			}
		}
		return null;
	}

	private static class InputStreamImpl extends InputStream {

		public InputStreamImpl(Object[] object) {
			// TODO Auto-generated constructor stub
		}

		@Override
		public int read() throws IOException {
			// TODO Auto-generated method stub
			return 0;
		}

	}

	static class FileOutput implements Output<OutputStream> {

		@Override
		public Class<?> getSupportedType() {
			return File.class;
		}

		@Override
		public OutputStream create(Stream stream) throws IOException {
			return new FileOut(File.createTempFile("fdsa", "tmp"),stream.fileConfig().append());
		}

		@Override
		public Object output(OutputStream object) {
			return ((FileOut)object).file;
		}

	}
	
	static class FileOut extends FileOutputStream{
		
		private File file;

		public FileOut(File file,boolean append) throws FileNotFoundException {
			super(file,append);
			this.file=file;
		}
		
	}

	static class BytesOutput implements Output<ByteArrayOutputStream> {

		@Override
		public Class<?> getSupportedType() {
			return byte[].class;
		}

		@Override
		public ByteArrayOutputStream create(Stream stream) throws IOException {
			return new ByteArrayOutputStream();
		}

		@Override
		public Object output(ByteArrayOutputStream object) {
			return object;
		}

	}

	static class StringOutput implements Output<StringWriter> {

		@Override
		public Class<?> getSupportedType() {
			return String.class;
		}

		@Override
		public StringWriter create(Stream stream) throws IOException {
			return new StringWriter();
		}

		@Override
		public Object output(StringWriter object) throws IOException {
			String rs = object.toString();
			object.close();
			return rs;
		}

	}
	
	static class StringToOutputTranslator implements Translator<String,OutputStream>{

		@Override
		public Class<String> getSourceType() {
			return String.class;
		}

		@Override
		public Class<OutputStream> getTargetType() {
			return OutputStream.class;
		}

		@Override
		public void transfer(String source, OutputStream target,Stream stream)  throws IOException{
			if(stream.outputCharset().length()>0){
				target.write(source.getBytes(stream.outputCharset()));
			}else{
				target.write(source.getBytes());
			}
		}
		
	}
	

	static class StringToWriterTranslator implements Translator<String,Writer>{

		@Override
		public Class<String> getSourceType() {
			return String.class;
		}

		@Override
		public Class<Writer> getTargetType() {
			return Writer.class;
		}

		@Override
		public void transfer(String source, Writer target,Stream stream)  throws IOException{
			target.write(source);
		}
		
	}

	static class BytesToWriterTranslator implements Translator<byte[],Writer>{

		@Override
		public Class<byte[]> getSourceType() {
			return byte[].class;
		}

		@Override
		public Class<Writer> getTargetType() {
			return Writer.class;
		}

		@Override
		public void transfer(byte[] source, Writer target,Stream stream)  throws IOException{
			if(stream.outputCharset().length()>0){
				target.write(new String(source,stream.outputCharset()));
			}else{
				target.write(new String(source));
			}
		}
		
	}

	static class BytesToOutputTranslator implements Translator<byte[],OutputStream>{

		@Override
		public Class<byte[]> getSourceType() {
			return byte[].class;
		}

		@Override
		public Class<OutputStream> getTargetType() {
			return OutputStream.class;
		}

		@Override
		public void transfer(byte[] source, OutputStream target,Stream stream)  throws IOException{
			target.write(source);
		}
		
	}

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		this.applicationContext=applicationContext;
	}

}
