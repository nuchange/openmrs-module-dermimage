package org.openmrs.module.dermimage.fragment.controller;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.io.FileUtils;
import org.openmrs.Patient;
import org.openmrs.api.PatientService;
import org.openmrs.ui.framework.SimpleObject;
import org.openmrs.ui.framework.annotation.SpringBean;
import org.openmrs.ui.framework.fragment.FragmentConfiguration;
import org.openmrs.ui.framework.fragment.FragmentModel;
import org.openmrs.util.OpenmrsUtil;
import org.springframework.web.bind.annotation.RequestParam;
import sun.misc.BASE64Decoder;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;


/**
 * Created by beapen on 28/12/15.
 *
 * Ref: https://github.com/openmrs/openmrs-module-orderentryui/blob/master/omod/src/main/java/org/openmrs/module/orderentryui/fragment/controller/patientdashboard/ActiveDrugOrdersFragmentController.java
 */
public class DermimageFragmentController {


    public void controller(FragmentConfiguration config,
                           @SpringBean("patientService") PatientService patientService,
                           FragmentModel model) throws Exception {
        // unfortunately in OpenMRS 2.1 the coreapps patient page only gives us a patientId for this extension point
        // (not a patient) but I assume we'll fix this to pass patient, so I'll code defensively
        Patient patient;
        config.require("patient|patientId");
        Object pt = config.getAttribute("patient");
        if (pt == null) {
            patient = patientService.getPatient((Integer) config.getAttribute("patientId"));
        } else {
            // in case we are passed a PatientDomainWrapper (but this module doesn't know about emrapi)
            patient = (Patient) (pt instanceof Patient ? pt : PropertyUtils.getProperty(pt, "patient"));
        }
        String sep = File.separator;
        // Folder in which images are saved
        File imgDir = new File(OpenmrsUtil.getApplicationDataDirectory() +
                sep + "patient_images" + sep +
                patient.getPatientId().toString().trim() + sep);

        if (!imgDir.exists()) {
            FileUtils.forceMkdir(imgDir);
        }
        ArrayList<String> fileNames = new ArrayList<String>(Arrays.asList(imgDir.list()));
        model.addAttribute("patient", patient);
        model.addAttribute("folder", imgDir);
        model.addAttribute("listOfFiles", fileNames);
        model.addAttribute("numberOfFiles", fileNames.size());

    }


    /**
     * @param patientId as String
     * @param type      as String   // Not implemented
     * @param image     as String
     * @return Object with Message: Added
     * @should return object with the message added
     */

    public Object saveWebcam(@RequestParam("patientId") String patientId,
                             @RequestParam("type") String type,
                             @RequestParam("image") String image) {
        // type (pixel data / serialized string) is not implemented
        // Always serialized string
        if (image != null) {

            try {
                BASE64Decoder decoder = new BASE64Decoder();
                byte[] decodedBytes = decoder.decodeBuffer(image);
                String sep = File.separator;
                File imgDir = new File(OpenmrsUtil.getApplicationDataDirectory() +
                        sep + "patient_images" + sep + patientId + sep);
                if (!imgDir.exists()) {
                    FileUtils.forceMkdir(imgDir);
                }
                String date = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss").format(new Date());
                FileOutputStream fos = new FileOutputStream(imgDir + sep
                        + date + ".png");
                fos.write(decodedBytes);
                fos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }


        }

        SimpleObject o = SimpleObject.create("message", "Image created!");

        return o;
    }


    /**
     * @param patientId as String
     * @param image     as String
     * @return Object with Message: Added
     * @should return object with the message added
     */

    public Object deleteImage(@RequestParam("patientId") String patientId,
                              @RequestParam("image") String image) {

        SimpleObject output;
        String sep = File.separator;
        File toDelete = new File(OpenmrsUtil.getApplicationDataDirectory() +
                sep + "patient_images" + sep + patientId.trim() + sep + image.trim());
        if (toDelete.delete()) {
            output = SimpleObject.create("message", "Success");
        } else {
            output = SimpleObject.create("message", "Error!");
        }
        return output;
    }

}
