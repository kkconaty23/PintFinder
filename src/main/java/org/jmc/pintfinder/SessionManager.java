package org.jmc.pintfinder;
//HOLD THE SESSION AS AN ID TO REFERENCE (LOGIN NAME..ETC)
public class SessionManager {
    private static String currentUserUID;

    public static void setCurrentUserUid(String uid){
        currentUserUID = uid;
    }

    public static String getCurrentUserUid(){
        return currentUserUID;
    }
}
