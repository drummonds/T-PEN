/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package imageLines;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import textdisplay.Folio;

/**
 *
 * @author jdeerin1
 */
public class PageImage extends HttpServlet {
   
    /** 
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code> methods.
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {
       OutputStream os;
       os = response.getOutputStream();
        try {


           if(request.getParameter("folio")!=null)
           {
               Folio f=new Folio(Integer.parseInt(request.getParameter("folio")));
               if(f.getArchive().compareTo("TPEN")==0)
               {
                response.setContentType("image/jpeg");
                System.out.print("loading image "+Folio.getRbTok("LOCALIMAGESTORE")+f.getPageName()+".jpg\n");
                BufferedImage img=ImageHelpers.readAsBufferedImage(Folio.getRbTok("LOCALIMAGESTORE")+f.getPageName()+".jpg");
                String url=("/usr/web/parker/"+f.getPageName());
                int height=2000;
                int width=(int) (img.getWidth() * ((double) 2000 / img.getHeight()));
                img=ImageHelpers.scale(img, height, width);
                ImageIO.write(img,"jpeg",os);
               }
               if(f.getArchive().compareTo("CCL")==0)
               {
                response.setContentType("image/jpeg");
                System.out.print(Folio.getRbTok("LOCALIMAGESTORE")+""+f.getImageName()+".jpg");
                System.out.flush();
                BufferedImage img=ImageHelpers.readAsBufferedImage(Folio.getRbTok("LOCALIMAGESTORE")+""+f.getImageName()+".jpg");
                String url=("/usr/web/parker/"+f.getPageName());
                ImageIO.write(img,"jpeg",os);
               }
               if(f.getArchive().compareTo("private")==0)
               {
                   response.setContentType("image/jpeg");
                System.out.print(Folio.getRbTok("uploadLocation")+""+f.getImageName()+".jpg");
                System.out.flush();
                BufferedImage img=ImageHelpers.readAsBufferedImage(f.getImageName()+".jpg");
                ImageIO.write(img,"jpeg",os);
               }
           }
           else
               response.sendError(400);
        }
        catch(SQLException e)
        {
            response.sendError(503);
        }
       finally {

                os.close();
        }
    } 

    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    /** 
     * Handles the HTTP <code>GET</code> method.
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {
        processRequest(request, response);
    } 

    /** 
     * Handles the HTTP <code>POST</code> method.
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {
        processRequest(request, response);
    }

    /** 
     * Returns a short description of the servlet.
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Short description";
    }// </editor-fold>

}