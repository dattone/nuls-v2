/**
 * MIT License
 * <p>
 * Copyright (c) 2017-2019 nuls.io
 * <p>
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * <p>
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package io.nuls.core.h2.interceptor;

import io.nuls.core.h2.transactional.annotation.Transaction;
import io.nuls.core.h2.utils.MybatisDbHelper;
import io.nuls.core.core.annotation.Interceptor;
import io.nuls.core.core.inteceptor.base.BeanMethodInterceptor;
import io.nuls.core.core.inteceptor.base.BeanMethodInterceptorChain;
import org.apache.ibatis.session.SqlSession;

import java.lang.reflect.Method;

/**
 * @author zhouwei
 * @date 2017/10/13
 */
@Interceptor(Transaction.class)
public class TransactionInterceptor implements BeanMethodInterceptor<Transaction> {

    private static ThreadLocal<Boolean> FLAG_HOLDER = new ThreadLocal<Boolean>() {
        @Override
        protected Boolean initialValue() {
            return false;
        }
    };

    @Override
    public Object intercept(Transaction annotation, Object object, Method method, Object[] params, BeanMethodInterceptorChain interceptorChain) throws Throwable {
        boolean flag = FLAG_HOLDER.get();
        Object result;

        if (!flag) {
            SqlSession sqlSession = null;
            try {
                FLAG_HOLDER.set(true);
                sqlSession = MybatisDbHelper.getSession();
                result = interceptorChain.execute(annotation, object, method, params);
                sqlSession.commit();
            } catch (Exception e) {
                if (sqlSession != null) {
                    sqlSession.rollback();
                }
                throw e;
            } finally {
                if (sqlSession != null) {
                    MybatisDbHelper.close(sqlSession);
                }
                FLAG_HOLDER.remove();
            }
        } else {
            result = interceptorChain.execute(annotation, object, method, params);
        }
        return result;
    }
}
