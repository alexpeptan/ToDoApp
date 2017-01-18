TODO app
========
The purpose of this app is to manage my own tasks!

* Tasks can be created (C)
* User login (authentication)
* Task created by user (authorization)

* Tasks can be viewed by anyone. (R)
* Expired tasks must be marked with red.
* Tasks can be assigned to a user in the system. (U) 
* Tasks can be updated (by assigned to or creator). (U)
* Tasks can be deleted. (D)                (functional behavior)
* Tasks can be deleted by creator          (non-functional behavior / AOP)
* Tasks cannot be deleted by anyone else   
* Tasks cannot be deleted with anonymous user

* Tasks will have basic validation in place. (C) -> javax validation 

To simplify the job, only listing will be done with templating, all others will
be done with HTTP calls.

* CRUD for _Task_ entity.


Resources
=========
* http://www.w3.org/Protocols/rfc2616/rfc2616-sec10.html
* http://www.w3.org/Protocols/rfc2616/rfc2616-sec9.html
* http://www.w3schools.com/tags/ref_urlencode.asp
* spring data opinionated queries!
* basic AUTH
* spring security