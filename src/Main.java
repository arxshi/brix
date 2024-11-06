import database.PostRepository;
import database.RuntimePostRepository;
import model.Post;
import model.builder.PostBuilder;
import service.PostService;
import service.RuntimePostService;
import service.ServiceManager;
import service.EditPostCommand;
import service.ArchivePostServiceDecorator;
import java.util.List;


import java.util.Date;
import java.util.Scanner;

public class Main {

    static PostRepository postRepo;
    static PostService postService;
    static ServiceManager serviceManager;

    public static void main(String[] args) {

        postRepo = new RuntimePostRepository();
        PostService runtimePostService = new RuntimePostService(postRepo);


        postService = new ArchivePostServiceDecorator(runtimePostService);

        serviceManager = new ServiceManager();
        serviceManager.registerService(postService);

        Scanner sc = new Scanner(System.in);
        mainMenu();
        while (sc.hasNextInt()) {
            int pressedKey = sc.nextInt();
            switch (pressedKey) {
                case 1 -> createPostMenu();
                case 2 -> displayAllPosts();
                case 3 -> archivePostMenu();
                case 4 -> editPostMenu();
                case 5 -> undoLastAction();
                case 6 -> displayArchivedPosts();
            }
        }
        sc.close();
    }

    private static void createPostMenu() {
        PostBuilder builder = new PostBuilder();
        Scanner sc = new Scanner(System.in);

        System.out.println("Input the title.");
        String title = sc.nextLine();
        System.out.println("Input the content.");
        String content = sc.nextLine();
        System.out.println("Input the name of poster.");
        String postedBy = sc.nextLine();

        builder.setCreatedAt(new Date());
        Post post = builder.setTitle(title).setContent(content).setPostedBy(postedBy).build();

        postService.createPost(post);
        System.out.println("Post created successfully.");
        mainMenu();
    }

    private static void displayAllPosts() {
        PostService service = serviceManager.getService(PostService.class);
        for (Post post : service.getAllPosts()) {
            System.out.println("---------");
            System.out.printf("Title: %s Id: %d%n", post.getTitle(), post.getId());
            System.out.printf("Content: %s%n", post.getContent());
            System.out.printf("Posted by: %s%n", post.getPostedBy());
            System.out.printf("Created at: %s%n", post.getCreatedAt().toString());
            System.out.println("---------");
        }
        if (service.getAllPosts().isEmpty()) {
            System.out.println("No posts yet.");
        }
        mainMenu();
    }

    private static void archivePostMenu() {
        Scanner sc = new Scanner(System.in);
        System.out.println("Input id of post to archive:");
        long id = sc.nextLong();

        Post post = postService.getPostById(id);
        if (post != null) {
            postService.deletePost(id);
            System.out.println("Post archived successfully.");
        } else {
            System.out.println("Post with id " + id + " does not exist.");
        }
        mainMenu();
    }

    private static void displayArchivedPosts() {
        List<Post> archivedPosts = postService.getArchivedPosts();
        if (archivedPosts.isEmpty()) {
            System.out.println("No archived posts.");
        } else {
            for (Post post : archivedPosts) {
                System.out.println("---------");
                System.out.printf("Title: %s Id: %d%n", post.getTitle(), post.getId());
                System.out.printf("Content: %s%n", post.getContent());
                System.out.printf("Posted by: %s%n", post.getPostedBy());
                System.out.printf("Created at: %s%n", post.getCreatedAt().toString());
                System.out.println("---------");
            }
        }
        mainMenu();
    }
    private static void editPostMenu() {
        Scanner sc = new Scanner(System.in);
        System.out.println("Enter the ID of the post you want to edit:");
        long id = sc.nextLong();
        sc.nextLine();
        System.out.println("Enter the new content:");
        String newContent = sc.nextLine();

        EditPostCommand editCommand = new EditPostCommand(postService, id, newContent);
        serviceManager.executeCommand(editCommand);

        System.out.println("Post edited successfully.");
        mainMenu();
    }

    private static void undoLastAction() {
        serviceManager.undoLastCommand();
        System.out.println("Last action undone.");
        mainMenu();
    }
    private static void mainMenu() {
        System.out.println("Press 1 to create post.");
        System.out.println("Press 2 to see all posts.");
        System.out.println("Press 3 to delete post.");
        System.out.println("Press 4 to edit post.");
        System.out.println("Press 5 to undo last action.");
        System.out.println("Press 6 to see bin.");
    }
}
