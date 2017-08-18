package org.camunda.bpm.engine.impl.db.orientdb;

import com.github.raymanrt.orientqb.query.Operator;

public class Parameter{
	public String name;
	public Operator op;
	public Object value;

	public Parameter( String n, Operator o, Object v){
		this.name = n;
		this.op = o;
		this.value = v;
	}
	public String toString(){
		return name+" " + op + " " + value;
	}
}
