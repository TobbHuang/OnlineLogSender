import net.sf.json.JSONObject;
import org.apache.commons.beanutils.converters.StringArrayConverter;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Created by huangtao on 2017/4/18.
 */
@WebServlet("/NotificationServlet")
public class NotificationServlet extends HttpServlet {

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doSend(request, response);
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doPost(request, response);
    }

    private void doSend(HttpServletRequest request, HttpServletResponse response) {
        JSONObject param = JSONObject.fromObject(request.getParameter("param"));

        String url = param.getString("url");
        String key = param.getString("key");
        int port = Integer.parseInt(param.getString("port"));

        System.out.println("初始化成功，信息： " + param);

        try {
            if (url == null || url.isEmpty() || key == null || key.isEmpty()) {
                JSONObject json = new JSONObject();
                json.element("status", "ERROR");
                json.element("msg", "Parameters are wrong.");
                response.setContentType("application/json; charset=utf-8");
                PrintWriter out = response.getWriter();
                out.print(json);
                return;
            } else {
                JSONObject json = new JSONObject();
                json.element("status", "OK");
                response.setContentType("application/json; charset=utf-8");
                PrintWriter out = response.getWriter();
                out.print(json);
                out.flush();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        LogSender sender = new LogSender(url, key, port, this);
        sender.doSend("2.0-1000-test.xes");
    }

}
