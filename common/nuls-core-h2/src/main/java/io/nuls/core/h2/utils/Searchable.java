/**
 * MIT License
 *
 * Copyright (c) 2017-2019 nuls.io
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package io.nuls.core.h2.utils;

import java.util.ArrayList;
import java.util.List;

/**
 * dao查询接口，封装查询语句工具类
 * @author zoro
 *
 */
public class Searchable {

	/**
     * 查询连接符，如：= != like
     */
    private List<Condition> operators;

    public Searchable() {
    	operators = new ArrayList<Condition>();
    }

    public Searchable(List<Condition> operators) {
    	this.operators = operators;
    }

    /**
     * 添加查询条件
     * @param c
     */
    public void addCondition(Condition c) {
    	this.operators.add(c);
    }


    public void addCondition(String key , SearchOperator operator, Object value) {
    	this.addCondition(new Condition(key,operator,value));
	}

    /**
     * 根据key删除某个条件
     * @param key
     */
    public void removeCondition(String key) {
    	Condition c = null;
    	for(int i=0; i<operators.size(); i++) {
    		c = operators.get(i);
    		if(c.getKey().equals(key)) {
    			operators.remove(i);
    			break;
    		}
    	}
    }

    /**
     * 根据key获取一个Condition
     * @param key
     * @return
     */
    public Condition getCondition(String key) {
    	Condition c = null;
    	for(int i=0; i<operators.size(); i++) {
    		c = operators.get(i);
    		if(c.getKey().equals(key)) {
    			return c;
    		}
    	}
    	return null;
    }
    
    
    public void removeAll() {
    	operators = new ArrayList<Condition>();
    }

	public List<Condition> getOperators() {
		return operators;
	}

	public void setOperators(List<Condition> operators) {
		this.operators = operators;
	}
}
