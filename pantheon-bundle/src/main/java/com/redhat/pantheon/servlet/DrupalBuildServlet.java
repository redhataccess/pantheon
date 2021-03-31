package com.redhat.pantheon.servlet;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.redhat.pantheon.asciidoctor.AsciidoctorPool;
import com.redhat.pantheon.asciidoctor.extension.SlingResourceIncludeProcessor;
import com.redhat.pantheon.model.assembly.TableOfContents;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.servlets.SlingAllMethodsServlet;
import org.apache.sling.servlets.annotations.SlingServletPaths;
import org.asciidoctor.Asciidoctor;
import org.asciidoctor.OptionsBuilder;
import org.asciidoctor.SafeMode;
import org.asciidoctor.ast.Document;
import org.asciidoctor.extension.IncludeProcessor;
import org.asciidoctor.extension.PreprocessorReader;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectLoader;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevTree;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.eclipse.jgit.treewalk.filter.PathFilter;
import org.jetbrains.annotations.NotNull;
import org.osgi.framework.Constants;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.servlet.Servlet;
import javax.servlet.ServletException;
import java.io.IOException;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Component(
        service = Servlet.class,
        property = {
                Constants.SERVICE_DESCRIPTION + "=Servlet which builds and delivers documents based on git parameters",
                Constants.SERVICE_VENDOR + "=Red Hat Content Tooling team"
        })
@SlingServletPaths(
        value = "/api/build"
)
public class DrupalBuildServlet extends SlingAllMethodsServlet {

    private AsciidoctorPool asciidoctorPool;

    @Activate
    public DrupalBuildServlet(@Reference AsciidoctorPool asciidoctorPool) {
        this.asciidoctorPool = asciidoctorPool;
    }

    @Override
    protected void doPost(@NotNull SlingHttpServletRequest request, @NotNull SlingHttpServletResponse response) throws ServletException, IOException {
        String repo = request.getParameter("repo");
        final String path = request.getParameter("path");
        String commitref = request.getParameter("commitref");
        String build_identifier = request.getParameter("build_identifier");

//        System.out.println("repo: " + repo);
//        System.out.println("path: " + path);
//        System.out.println("commitref: " + commitref);
//        System.out.println("build_identifier: " + build_identifier);

        Map<String, Object> payload = new HashMap<>();
        payload.put("build_identifier", build_identifier);
        payload.put("path", path);
        payload.put("repo", repo);

        Path tempdir = Files.createTempDirectory("teamRyan-");
        try (Git git = Git.cloneRepository().setURI(repo).setDirectory(tempdir.toFile()).call()) {
            ObjectId commitId = git.getRepository().resolve(commitref);
            RevWalk walk = new RevWalk(git.getRepository());
            RevCommit commit = walk.parseCommit(commitId);
            RevTree tree = commit.getTree();
//            System.out.println("Tree: " + tree);

            final TreeWalk treeWalk = new TreeWalk(git.getRepository());
            treeWalk.addTree(tree);
            treeWalk.setRecursive(true);
            treeWalk.setFilter(PathFilter.create(path));
            if (!treeWalk.next()) {
                throw new ServletException("Could not find file " + path + " in repository " + repo + " and commit " + commitref);
            }

            ObjectId objectId = treeWalk.getObjectId(0);
            ObjectLoader loader = git.getRepository().open(objectId);

            String content = new String(loader.getBytes());

//            System.out.println(content);

            OptionsBuilder ob = OptionsBuilder.options()
                    // we're generating html
                    .backend("html")
                    // no physical file is being generated
                    .toFile(false)
                    // allow for some extra flexibility
                    .safe(SafeMode.UNSAFE) // This probably needs to change
                    .inPlace(false)
                    // Generate the html header and footer
                    .headerFooter(true);

            Asciidoctor asciidoctor = asciidoctorPool.borrowObject();
            try {
                asciidoctor.javaExtensionRegistry().includeProcessor(
                        new IncludeProcessor() {

                            @Override
                            public boolean handles(String s) {
                                return true;
                            }

                            @Override
                            public void process(Document document, PreprocessorReader preprocessorReader, String target, Map<String, Object> map) {
                                // FIXME - MASSIVE SHORTCUT - no idea if this works for anything but the simplest of include scenarios
                                // Lots of assumptions made here - that we can reuse jgit objects, that all includes are resolved relative to
                                // document root (I think so, but it's been awhile since I've had to think about this...), stuff like that.
                                // This isn't heavily tested.
                                String[] pathPieces = target.split("/");
                                String parentPath = Arrays.stream(pathPieces).limit(pathPieces.length - 1).collect(Collectors.joining("/"));
                                treeWalk.setFilter(PathFilter.create(parentPath + (parentPath.isEmpty() ? "" : "/") + target));

                                String includeContent = "Invalid include: " + target;
                                try {
                                    if (treeWalk.next()) {
                                        ObjectId objectId = treeWalk.getObjectId(0);
                                        ObjectLoader loader = git.getRepository().open(objectId);

                                        includeContent = new String(loader.getBytes());
                                    }
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }

                                preprocessorReader.push_include(includeContent, target, target, 1, map);
                            }
                        });



                String html = asciidoctor.convert(content, ob.get());
//                System.out.println(html);
                payload.put("html", html);
            } finally {
                asciidoctorPool.returnObject(asciidoctor);
            }
        } catch (GitAPIException e) {
            throw new ServletException(e);
        }

        response.setContentType("application/json");
        Writer w = response.getWriter();
        w.write(new ObjectMapper().writer().writeValueAsString(payload));
    }
}
