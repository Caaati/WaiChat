package com.zafu.waichat.util;

/**
 *@ClassName ResultCode
 *@Description 请求结果状态枚举
 *@Author Zhiming.Ma
 *@Date 2020/10/20
 */
public enum ResponseEnum {
	/**
	 *
	 */
	ERROR(100,"请求失败!"),
	SUCCESS(200,"请求成功!"),
	INSERT_ERROR(101,"数据插入失败!"),
	UPDATE_ERROR(102,"数据更新失败!"),
	DELETE_ERROR(103,"数据删除失败!"),
	SELECT_ERROR(104,"数据查询失败!");

	private final int code;
	private final String message;

	ResponseEnum(int code, String message){
		this.code = code;
		this.message = message;
	}

	public int getCode (){
		return this.code;
	}

	public String getMessage(){
		return this.message;
	}

}
