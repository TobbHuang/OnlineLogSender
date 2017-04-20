import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;

import javax.servlet.GenericServlet;
import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by huangtao on 2017/4/18.
 */
public class LogSender {

    private String url;
    private String key;
    private int port;

    private GenericServlet servlet;

    public LogSender(String url, String key, int port, GenericServlet servlet){
        this.url = url;
        this.key = key;
        this.servlet = servlet;
        this.port = port;
    }

    public void doSend(String logName){
        new Thread(() -> {
            // 日志传送采用socket长连接
            try {
                List<JSONArray> logData = buildLogList(logName);
                Socket socket = new Socket(url, port);
                BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                PrintWriter writer = new PrintWriter(socket.getOutputStream());

                for(JSONArray trace : logData){
                    System.out.println("Send: " + trace);
                    JSONObject jsn = new JSONObject();
                    jsn.put("log", trace);
                    jsn.put("key", key);
                    writer.println(jsn);
                    writer.flush();

                    JSONObject sendResultJsn = JSONObject.fromObject(reader.readLine());
                    if(!sendResultJsn.getString("status").equals("OK")){
                        System.out.println("发送失败");
                        return;
                    }
                }
                writer.println("over");
                writer.flush();

                reader.close();
                writer.close();
                socket.close();
                System.out.println("发送完成");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }

    private List buildLogList(String logName) {
        List<JSONArray> traceArray = new ArrayList<>();
        try {
            String realPath = servlet.getServletContext().getRealPath("/WEB-INF/logs/");
            System.out.println(realPath);
            XLog log = XLogManager.readLog(new FileInputStream(realPath + logName), logName);
            for (XTrace trace : log) {
                JSONArray jsnArray = new JSONArray();
                for (XEvent event : trace) {
                    JSONObject jsn = new JSONObject();
                    jsn.put("EventName", XLogManager.getEventName(event));
                    jsnArray.add(jsn);
                }
                traceArray.add(jsnArray);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return traceArray;
    }

}
