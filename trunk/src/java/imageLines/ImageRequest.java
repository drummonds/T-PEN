/*
 * @author Jon Deering
Copyright 2011 Saint Louis University. Licensed under the Educational Community License, Version 2.0 (the "License"); you may not use
this file except in compliance with the License.

You may obtain a copy of the License at http://www.osedu.org/licenses/ECL-2.0

Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions
and limitations under the License.
 */

package imageLines;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import textdisplay.DatabaseWrapper;
import textdisplay.Folio;

/**Handles logging of image requests, tracking of success and failure as well as load times, so we can set user expectations.*/
public class ImageRequest {
    private long startTime;
    private Boolean cacheHit;
    private int id;
    /**
     * A new image request.
     * @param folioNumber requested folio number
     * @param UID user who requested it. 0 means it was an internal TPEN request to service parsing and the like.
     * @throws SQLException
     */
    public ImageRequest(int folioNumber, int UID) throws SQLException
    {
    System.out.print("saving record for "+folioNumber+" "+UID+"\n");
    startTime=System.currentTimeMillis();
    Folio f=new Folio(folioNumber);
    cacheHit=f.isCached();
    String query="insert into imageRequest(UID,folio,cacheHit,elapsedTime,date,succeeded,msg) values (?,?,?,?,NOW(),?,?)";
    Connection j = null;
PreparedStatement ps=null;
        try {
        j=DatabaseWrapper.getConnection();
        ps=j.prepareStatement(query,PreparedStatement.RETURN_GENERATED_KEYS);
        ps.setInt(1, UID);
        ps.setInt(2, folioNumber);
        ps.setBoolean(3, cacheHit);
        ps.setInt(4, 0);
        ps.setBoolean(5, false);
        ps.setString(6, "started");
        ps.execute();
        ResultSet rs=ps.getGeneratedKeys();
        if(rs.next())
            this.id=rs.getInt(1);
        else
            this.id=-1;
        }
        finally{
            DatabaseWrapper.closeDBConnection(j);
            DatabaseWrapper.closePreparedStatement(ps);
        }
    }
/**
 * If the image request was successfully serviced, mark it as such.
 * @throws Exception
 */
public void completeSuccess() throws Exception
    {
        if(this.id>0)
        {
        long timeElapsed=System.currentTimeMillis()-startTime;
        String query="update imageRequest set elapsedTime=?, succeeded=true, msg='' where id=?";
        Connection j = null;
PreparedStatement ps=null;
        try {
        j=DatabaseWrapper.getConnection();
        ps=j.prepareStatement(query);
        ps.setInt(1, (int) timeElapsed);
        ps.setInt(2, id);
        ps.execute();
            }
        finally{
            DatabaseWrapper.closeDBConnection(j);
            DatabaseWrapper.closePreparedStatement(ps);
        }
        }
 else{
     throw new Exception("Missing request ID.");
    }
    }
/**
 * The image couldnt be delivered. Indicate that in our records along with any reason we can provide
 * @param msg Anything about why the image delivery failed. 
 * @throws Exception
 */
public void completeFail(String msg) throws Exception
    {
        if(this.id>0)
        {
        long timeElapsed=System.currentTimeMillis()-startTime;
        String query="update imageRequest set elapsedTime=?, succeeded=false, msg=? where id=?";
        Connection j = null;
PreparedStatement ps=null;
        try {
        j=DatabaseWrapper.getConnection();
        ps=j.prepareStatement(query);
        ps.setInt(1, (int) timeElapsed);
        ps.setString(2, msg);
        ps.setInt(3, id);
        ps.execute();
            }
        finally{
            DatabaseWrapper.closeDBConnection(j);
            DatabaseWrapper.closePreparedStatement(ps);
        }
        }
 else{
     throw new Exception("Missing request ID.");
    }
    }
    /**
     * Look at the last 10 successful non cache hit image requests for an archive and compute the mean turnaround time.
     * @param archive Name of the archive you want to check on. 
     * @return
     * @throws SQLException
     */
    public static int getAverageElapsedTime(String archive) throws SQLException
    {
        String query="select elapsedTime from imageRequest join folios on imageRequest.folio=folios.pageNumber where archive=? and cacheHit=false order by date limit 10";
        Connection j = null;
PreparedStatement ps=null;
        try {
        j=DatabaseWrapper.getConnection();
        ps=j.prepareStatement(query);
        ps.setString(1, archive);
        ResultSet rs=ps.executeQuery();
        int sum=0;
        int count=0;
        while(rs.next())
        {
            sum+=rs.getInt(1);
            count++;
        }
        if(count==0)
            return 0;
        return sum/count;
        }
         finally{
            DatabaseWrapper.closeDBConnection(j);
            DatabaseWrapper.closePreparedStatement(ps);
        }
    }
}