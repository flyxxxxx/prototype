package org.prototype.business;

import java.lang.reflect.InvocationTargetException;
import java.sql.SQLException;

import org.prototype.annotation.Catch;
import org.prototype.annotation.Message;
import org.prototype.core.PrototypeStatus;
import org.springframework.context.MessageSource;
import org.springframework.transaction.annotation.Transactional;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * 业务类
 * 
 * @author lj
 *
 */
@BusinessDefine(sync = { @BusinessMethod(value = "validate", readOnly = true), @BusinessMethod(value = "business"),
		@BusinessMethod(value = "after", transaction = false),
		@BusinessMethod(value = "end", transaction = false) }, async = {
				@BusinessMethod(value = "async", overload = true) })
@Slf4j
public class Business {
	
	/**
	 * 异常消息
	 */
	public static final String MSG_EXCEPTION="exception";
	public static final String MSG_REJECT="reject";
	public static final String  MSG_TIMEOUT="timeout";

	public static final int SUCCESS = 1;
	public static final int ERROR = 0;
	public static final int ERROR_SQL = -1;
	public static final int TIMEOUT = -2;
	public static final int REJECT = -3;
	@Output( @Prop(desc = "结果"))
	@Getter
	protected int result = SUCCESS;
	@Output(@Prop(desc = "原因"))
	@Getter
	protected String reason;

	@Output(@Prop(desc = "验证错误"))
	@Getter
	protected String errors;

	/**
	 * 子类如果重写此方法，并定义了Chain/OverloadAsync注解，则业务类的执行按子类的这两个注解为准（如果仅定义了其中一个注解，则另一个注解由父类定义）
	 */
	@Catch // 将异常交给executeException方法处理
	@Transactional// 在此处定义事务表示所有业务方法（除异步方法外）均在一个事务中处理事务.默认情况下各业务方法有自己的事务管理.
	public void execute() {
		// do nothing
	}

	/**
	 * 结果小于0，并且未输出原因时，从消息资源中查找本地化信息
	 * 
	 * @param source
	 */
	protected void end(MessageSource source) {
		if (reason != null && result < 0) {
			source.getMessage("error." + result, new Object[0], "UNKNOW ERROR", PrototypeStatus.getStatus().getLocale());
		}
	}

	/**
	 * execute方法的执行异常，可以自定义更多的异常类型
	 * 
	 * @param e
	 *            异常
	 */
	public final void executeException(SQLException e) {
		result = ERROR_SQL;
		log.warn("SQL error", e);
		Message.getSubject().onNext(new Message(MSG_EXCEPTION, getClass().getName(), e));
	}

	/**
	 * 调用另一方法时的异常
	 * 
	 * @param e
	 */
	public final void executeException(InvocationTargetException e) {
		Throwable t = e.getTargetException();
		if (t instanceof Exception) {
			executeException((Exception) t);
		} else {
			result = ERROR_SQL;
			log.warn("SQL error", t);
			Message.getSubject().onNext(new Message(MSG_EXCEPTION, getClass().getName(), e));
		}
	}

	/**
	 * execute方法的执行异常。 <br>
	 * 不建议在异常处理中调用其它服务来记录异常信息，而是通过消息机制统一收集处理(Subscribe注解).
	 * 可以通过{@link org.prototype.core.PrototypeStatus#getStatus()}获取当前执行的实例及其它状态.
	 * @param e
	 *            异常
	 */
	public final void executeException(Exception e) {
		result = ERROR;
		log.warn("Unknow error", e);
		Message.getSubject().onNext(new Message(MSG_EXCEPTION, getClass().getName(), e));
	}
	
	void setResult(int result){
		this.result=result;
	}
	
	void addValidateError(String error){
		if(this.errors==null){
			errors=error;
		}else{
			errors=errors+","+error;
		}
	}
}
