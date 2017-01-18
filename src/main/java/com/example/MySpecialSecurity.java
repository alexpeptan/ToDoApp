package com.example;

import com.example.domain.Task;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Component("mySecurity")
public class MySpecialSecurity {

    @Autowired
    private TaskRepository taskRepository;

	@Transactional(propagation = Propagation.MANDATORY)
    public boolean isTaskCreator(Long taskId) {
        Task t = taskRepository.findOne(taskId);
        if (t == null) {
            return true;
        } else {
            User u = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            return Long.toString(t.getAuthor().getId()).equals(u.getUsername());
        }
    }

    @Transactional(propagation = Propagation.MANDATORY)
    public boolean isTaskAssignee(Long taskId) {
        Task t = taskRepository.findOne(taskId);
        if(t == null){
            return true;
        } else {
            User crtUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            return crtUser.getUsername().equals(String.valueOf(t.getAssignee().getId()));
        }
    }
}
