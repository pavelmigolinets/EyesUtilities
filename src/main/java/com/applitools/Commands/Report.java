package com.applitools.Commands;

import com.applitools.obj.Batches;
import com.applitools.obj.PathGenerator;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.apache.velocity.runtime.resource.loader.FileResourceLoader;
import org.apache.velocity.tools.generic.NumberTool;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.util.HashMap;

@Parameters(commandDescription = "Prepare report based on a template")

public class Report extends ResultsAPI {

    @Parameter(names = {"-t", "--template"}, description = "Template file.")
    private String templFileName = "report.templ";

    @Parameter(names = {"-d", "--destination"}, description = "Output folder+file destination.")
    private String reportoutfile = "report.html";

    @Parameter(names = {"-rt", "--title"}, description = "Report title for display purposes")
    private String reportTitle = "";

    private File templFile = null;

    private PathGenerator outPathGenerator = new PathGenerator("{report_root}/Artifacts/{batch_id}/{test_id}/file:{step_index}_{step_tag}_{artifact_type}.{file_ext}");

    public void run() throws Exception {
        templFile = new File(templFileName);
        Velocity.setProperty("file.resource.loader.class", FileResourceLoader.class.getName());
        Velocity.setProperty("file.resource.loader.path", templFile.getAbsoluteFile().getParent());
        Velocity.init();
        VelocityContext context = createContext();

        if (context == null) {
            System.out.printf("No information or unable to read it");
            return;
        }

        StringWriter writer = getReportStream(context);
        writeToFile(writer);
    }

    private void writeToFile(StringWriter writer) throws IOException {
        File report = new File(reportoutfile);
        FileOutputStream stream = new FileOutputStream(report);

        stream.write(writer.toString().getBytes());
        stream.flush();
        stream.close();
    }

    private StringWriter getReportStream(VelocityContext context) {
        StringWriter sw = new StringWriter();
        Template template = Velocity.getTemplate(templFile.getName());
        template.merge(context, sw);
        sw.flush();
        return sw;
    }

    private HashMap<String, String> getSelfPathParams() {
        HashMap<String, String> params = new HashMap<String, String>();
        params.put("report_root", new File(reportoutfile).getAbsoluteFile().getParentFile().getPath());
        params.put("user_root", new File(System.getProperty("user.dir")).getAbsolutePath());
        params.put("workdir_root", new File("").getAbsolutePath());
        params.put("artifacts", "artifacts");
        return params;
    }

    private VelocityContext createContext() throws IOException {
        PathGenerator baseGenerator = outPathGenerator.build(getSelfPathParams());
        VelocityContext context = new VelocityContext();
        Batches batches = new Batches(getUrls(), viewKey, baseGenerator);
        if (batches.size() == 0)
            return null;
        context.internalPut("batches", batches);
        context.internalPut("numberTool", new NumberTool());
        context.internalPut("title", reportTitle);
        //context.internalPut("server_url", getUrl().getServerAddress());
        return context;
    }
}
