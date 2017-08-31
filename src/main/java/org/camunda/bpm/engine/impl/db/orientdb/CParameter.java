package org.camunda.bpm.engine.impl.db.orientdb;

import com.github.raymanrt.orientqb.query.Operator;

public class CParameter{
	public String name;
	public Operator op;
	public Object value;
	public boolean noCheck=false;

	public CParameter( String n, Operator o, Object v){
		this( n, o, v, false );
	}
	public CParameter( String n, Operator o, Object v,boolean noc){
		this.name = n;
		this.op = o;
		this.value = v;
		this.noCheck = noc;
	}
	public String toString(){
		return String.format(op.toString(),name,value);
	}
}
