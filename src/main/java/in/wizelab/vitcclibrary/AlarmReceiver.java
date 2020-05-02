package in.wizelab.vitcclibrary;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.ContextCompat;
import android.widget.Toast;

public class AlarmReceiver extends BroadcastReceiver {

    Context context;
    @Override
    public void onReceive(Context context, Intent intent) {
        // For our recurring task, we'll just display a message
        //Toast.makeText(context, "Some books might be due soon", Toast.LENGTH_SHORT).show();
        this.context=context;
        sendNotification("Open the app to see details.");
    }

    private void sendNotification(String message){

        Intent intent = new Intent(context, MainActivity.class);
        PendingIntent pIntent = PendingIntent.getActivity(context, 0, intent, 0);

        Notification n  = new Notification.Builder(context)
                .setContentTitle("Some books might be DUE SOON")
                .setContentText(message)
                .setSmallIcon(R.drawable.notification_icon)
                .setContentIntent(pIntent)
                .setColor(ContextCompat.getColor(context, R.color.green_theme))
                .setAutoCancel(true).getNotification();
        NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(context.NOTIFICATION_SERVICE);
        notificationManager.notify(0, n);
    }

    public static void cancelNotification(Context ctx, int notifyId) {
        String ns = Context.NOTIFICATION_SERVICE;
        NotificationManager nMgr = (NotificationManager) ctx.getSystemService(ns);
        nMgr.cancel(notifyId);
    }

}