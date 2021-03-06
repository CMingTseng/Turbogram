package org.telegram.messenger.voip;

import android.annotation.TargetApi;
import android.net.ConnectivityManager;
import android.net.LinkAddress;
import android.net.LinkProperties;
import android.net.Network;
import android.os.Build.VERSION;
import com.google.android.exoplayer2.extractor.ts.PsExtractor;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;
import org.telegram.messenger.ApplicationLoader;
import org.telegram.messenger.FileLog;

public class JNIUtilities {
    @TargetApi(23)
    public static String getCurrentNetworkInterfaceName() {
        ConnectivityManager cm = (ConnectivityManager) ApplicationLoader.applicationContext.getSystemService("connectivity");
        Network net = cm.getActiveNetwork();
        if (net == null) {
            return null;
        }
        LinkProperties props = cm.getLinkProperties(net);
        if (props != null) {
            return props.getInterfaceName();
        }
        return null;
    }

    public static String[] getLocalNetworkAddressesAndInterfaceName() {
        ConnectivityManager cm = (ConnectivityManager) ApplicationLoader.applicationContext.getSystemService("connectivity");
        String ipv4;
        String ipv6;
        InetAddress a;
        if (VERSION.SDK_INT >= 23) {
            Network net = cm.getActiveNetwork();
            if (net == null) {
                return null;
            }
            LinkProperties linkProps = cm.getLinkProperties(net);
            if (linkProps == null) {
                return null;
            }
            ipv4 = null;
            ipv6 = null;
            for (LinkAddress addr : linkProps.getLinkAddresses()) {
                a = addr.getAddress();
                if (a instanceof Inet4Address) {
                    if (!a.isLinkLocalAddress()) {
                        ipv4 = a.getHostAddress();
                    }
                } else if (!(!(a instanceof Inet6Address) || a.isLinkLocalAddress() || (a.getAddress()[0] & PsExtractor.VIDEO_STREAM_MASK) == PsExtractor.VIDEO_STREAM_MASK)) {
                    ipv6 = a.getHostAddress();
                }
            }
            return new String[]{linkProps.getInterfaceName(), ipv4, ipv6};
        }
        try {
            Enumeration<NetworkInterface> itfs = NetworkInterface.getNetworkInterfaces();
            if (itfs == null) {
                return null;
            }
            while (itfs.hasMoreElements()) {
                NetworkInterface itf = (NetworkInterface) itfs.nextElement();
                if (!itf.isLoopback() && itf.isUp()) {
                    Enumeration<InetAddress> addrs = itf.getInetAddresses();
                    ipv4 = null;
                    ipv6 = null;
                    while (addrs.hasMoreElements()) {
                        a = (InetAddress) addrs.nextElement();
                        if (a instanceof Inet4Address) {
                            if (!a.isLinkLocalAddress()) {
                                ipv4 = a.getHostAddress();
                            }
                        } else if (!(!(a instanceof Inet6Address) || a.isLinkLocalAddress() || (a.getAddress()[0] & PsExtractor.VIDEO_STREAM_MASK) == PsExtractor.VIDEO_STREAM_MASK)) {
                            ipv6 = a.getHostAddress();
                        }
                    }
                    return new String[]{itf.getName(), ipv4, ipv6};
                }
            }
            return null;
        } catch (Exception x) {
            FileLog.e(x);
            return null;
        }
    }
}
