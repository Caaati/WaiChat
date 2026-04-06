package com.zafu.waichat.util;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 全局返回对象
 *@className Result
 *@author Zhiming.Ma
 *@date 2020/10/20
 */
@Schema(description = "返回对象")
public class Result<T> {

	@Schema(description = "返回状态")
	public int code;

	@Schema(description = "返回描述")
	public String message;

	@Schema(description = "返回数据")
	public T data;

	public void setCode(int code) {
		this.code = code;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public void setResultCode(ResponseEnum responseEnum) {
		this.code = responseEnum.getCode();
		this.message = responseEnum.getMessage();
	}

	public static Result<Void> success() {
		Result<Void> Result = new Result<>();
		Result.setResultCode(ResponseEnum.SUCCESS);
		return Result;
	}

	public static <T> Result<T> success(T data) {
		Result<T> Result = new Result<>();
		Result.setResultCode(ResponseEnum.SUCCESS);
		Result.data = data;
		return Result;
	}

	public static <T> Result<T> success(ResponseEnum responseEnum) {
		Result<T> Result = new Result<>();
		Result.setResultCode(responseEnum);
		return Result;
	}

	public static Result<Void> error() {
		Result<Void> Result = new Result<>();
		Result.setResultCode(ResponseEnum.ERROR);
		return Result;
	}

	public static Result<Void> error(String message) {
		Result<Void> Result = new Result<>();
		Result.setCode(100);
		Result.setMessage(message);
		return Result;
	}

	public static Result<Void> error(ResponseEnum responseEnum) {
		Result<Void> Result = new Result<>();
		Result.setCode(responseEnum.getCode());
		Result.setMessage(responseEnum.getMessage());
		return Result;
	}

}

