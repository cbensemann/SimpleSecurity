package nz.co.nomadconsulting.eesecurity.util;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


public class ThreadLocalUtils {

    public static ThreadLocal<HttpServletRequest> currentRequest = new ThreadLocal<HttpServletRequest>();

    public static ThreadLocal<HttpServletResponse> currentResponse = new ThreadLocal<HttpServletResponse>();
}
