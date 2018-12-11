package org.camunda.bpm.engine.impl.db.orientdb;

import com.github.raymanrt.orientqb.query.Operator;

public class SingleExpression{
	public String op;
	public String value;
	public String valueField;

	public SingleExpression( String op , String v, String vf){
		this.op = op;
		this.value = v;
		this.valueField = vf;
	}
	public String getValue(){
		return value;
	}
	public String getValueField(){
		return valueField;
	}
	public String getOp(){
		return op;
	}
}
