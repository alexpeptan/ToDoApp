package com.example;


import com.example.domain.Task;
import com.example.domain.User;
import com.example.domain.vo.TaskVO;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.Random;

@RunWith(SpringRunner.class)
@SpringBootTest
@Transactional
@ActiveProfiles({ "in_memory" })
public class TaskServiceTest {
    @Autowired
    private TaskService service;
    @Autowired
    private UserService userService;
    @Autowired
    private AuthenticationManager authentication;

    private User existingUser;
	private Task existingTask;

    @Before
    public void createUserBeforeEachTest() {
        existingUser = new User("alex", "adelina");
        userService.create(existingUser);
        // TODO check these suggestions, which one fits :)
        // userService.create(String username, String password): User
        // userService.create(UserDTO user): User

	    // Create task
	    TaskVO vo = TaskVO.builder().message("Sample").assigneeId(existingUser.getId()).build();
	    existingTask = SecurityContext.executeInUserContext(existingUser, u -> service.create(vo));
    }

    @Test(expected = AccessDeniedException.class)
    public void shouldNotBeAbleToDeleteTaskWhenAnonymous() throws Exception {
        // Delete task
        try {
            SecurityContextHolder.getContext().setAuthentication(new AnonymousAuthenticationToken("(anon)", "(anon)", Collections.singletonList(new SimpleGrantedAuthority("(anon)"))));
            /* action */
            service.delete(existingTask.getId());
            /* action */
        } catch (AccessDeniedException e) {
            // Check if task still exists
            Assert.assertNotNull(service.getTaskById(existingTask.getId()));
            throw e;
        }
    }

    @Test
    public void shouldDeleteTaskWhenCalledByOwner() throws Exception {
        // Delete task
        loginAs(existingUser);
        /* action */
        service.delete(existingTask.getId());
        /* action */

        // Check if task still exists
        Assert.assertNull(service.getTaskById(existingTask.getId()));
    }


    private void loginAs(User user){
        final UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(user.getUserName(), user.getPassword());
        SecurityContextHolder.getContext().setAuthentication(token);
    }

    @Test(expected = AccessDeniedException.class)
    public void shouldNotDeleteTaskWhenCalledBySomeOneElseThanOwner() throws Exception {
        // Delete task
        User badBoy = new User("alex badBoy", "adelina");
        userService.create(badBoy);
        try {
            loginAs(badBoy);
            /* action */
            service.delete(existingTask.getId());
            /* action */
        } catch (AccessDeniedException e) {
            // Check if task still exists
            Assert.assertNotNull(service.getTaskById(existingTask.getId()));
            throw e;
        }
    }

    private Task reAssign(Task task, User newUser) throws AccessDeniedException{
        return service.update(task.getId(), TaskVO.builder().message(task.getMessage()).
            assigneeId(newUser.getId()).build());
    }


    // Template method!!! - executeInUserContext
    @Test  // Super lizibil!
    public void shouldReassignWhenCalledByOwnerAndCreator() { // fails to check that test users really exist in the Database!!!  // mai multe teste -> Refactor!
        // reassign as a creator and owner
        User newOwner = new User("The new guy", "123456");
        userService.create(newOwner);
        try {
//            loginAs(existingUser);
//            reAssign(existingTask, newOwner);// and repeat!
            SecurityContext.executeInUserContext(existingUser, () -> reAssign(existingTask, someUser()));

        } catch (AccessDeniedException ex) {
            Assert.fail("Creator and Owner was not able to reassign task");
        }

//        Assert.assertNotEquals("Task reassignment failed", existingTask.getAssignee(), newOwner);
        Assert.assertNotEquals("Task reassignment failed", existingTask.getAssignee(), existingUser);
    }

    @Test
    public void shouldReassignWhenCalledByCreator() {
        try {
            // Build Precondition - task creator is not the owner of the task
            Assert.assertTrue("@Before Precondition not met", existingTask.getAssignee().equals(existingUser));
            SecurityContext.executeInUserContext(existingUser, u -> reAssign(existingTask, someUser()));
            Assert.assertTrue("The task was not successfully reassigned", !existingTask.getAssignee().equals(existingUser));

            // reassign as a creator, but not the owner, back to himself
            SecurityContext.executeInUserContext(existingUser, u -> reAssign(existingTask, u));
            Assert.assertTrue("The creator failed to reassign the task to itself", existingTask.getAssignee().equals(existingUser));
        } catch (AccessDeniedException ex) {
            Assert.fail("The creator should always be permitted to reassign the task");
        }
    }

    @Test(expected = AccessDeniedException.class)
    public void shouldFailToReassignWhenCalledBySomeoneElseThanCreatorOrOwner() {
        // Build Precondition - task owner is not its creator
        try {
            SecurityContext.executeInUserContext(existingUser, u -> reAssign(existingTask, someUser()));
        } catch (AccessDeniedException ex) {
            Assert.fail("Reassign by creator failed");
        }

        SecurityContext.executeInUserContext(someUser(), u -> reAssign(existingTask, someUser()));
    }

     @Test
     public void shouldReassignWhenCalledByOwner() {
         // reassign as a owner, but not creator
         User newOwner = someUser();
         try {
             // Precondition - the owner will need to differ from the creator

             SecurityContext.executeInUserContext(existingUser, u -> reAssign(existingTask, newOwner));
         } catch (AccessDeniedException ex) {
             Assert.fail("Reassign by creator failed");
         }

         try {
             SecurityContext.executeInUserContext(newOwner, u -> reAssign(existingTask, someUser()));
         } catch (AccessDeniedException ex) {
             Assert.fail("Reassign by owner failed");
         }

         Assert.assertNotEquals("Owner should have been changed", existingTask.getAssignee(), newOwner);
     }

    @Test(expected = AccessDeniedException.class)
    public void shouldNotReassignTaskAsAnonymous() {
        SecurityContextHolder.getContext().setAuthentication(new AnonymousAuthenticationToken("(anon)", "(anon)",
                Collections.singletonList(new SimpleGrantedAuthority("(anon)"))));

        service.update(existingTask.getId(), TaskVO.builder().message(existingTask.getMessage()).
                assigneeId(existingUser.getId()).build());
    }

    private User someUser() {
        User newOwner = new User("The new guy" + String.valueOf(new Random().nextInt(1000)), "123456");
        userService.create(newOwner);

        return newOwner;
    }
}
