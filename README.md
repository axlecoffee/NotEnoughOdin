# NotEnoughOdin
A lunar client designed implementation of OdinClient 1.3.1 And NotEnoughUpdates 2.5.0 
![image](https://github.com/user-attachments/assets/1229e5e9-ca36-47ea-84e8-096b376c4715)
![image](https://github.com/user-attachments/assets/e98caf66-4bb5-4286-9a0a-ced2a74cb0f6)
![image](https://github.com/user-attachments/assets/e32243ce-5b49-4e34-a9c6-033171139bbd)


# Guide

to install, simply compile this and place the jar in `~\.lunarclient\offline\multiver\overrides`
it must have the EXACT same name as your current NEU jar (which is found one dir up)
in my case it was NEU-v1_8-2.5.0

want to do the same?

any new "mod" you want to add has to be added inside of the notenoughupdates package
```
src/main/java/io/github/moulberry/notenoughupdates
```
and started from NotEnoughUpdates (simple enough) 

Comodore or however its spelt, Essential etc, all WILL fail to load you MUST use normal implimentations 
^ Ive yet to do custom fonts (odin's didnt work gg) 
I switched NEU's kotlin from 1.8x to 1.9x for odin so id assume 2x would also be fine
uh commands are annoying so currently to open the odin gui u gotta use `/neuah` (shizo ik) 

ALL CODE (aside from the TINY bit i edited) here is NOT owned by me and is licenced to NotEnoughUpdates and Odin 

^ also gl on implimenting odin legit version (i cba) 

regarding the loading this is what ive learned

you cant use tweakers
lunar calls itself "ichor" 
if a class/mixin is MISSING it crashes 
if a file is outside of moulberry dir it cannot access net.minecraft 
no URLclassloading since its technically java 17 (you compile with SDK 8 still) 

currently not many odin features work im working on it

dm me for help or whatever @axle.coffee on discord

proof of concept educational purposes only dont actually use this LOL 
(finally ended the "you cant add your own mods to lunar 1.8.9 no custom launcher) 

# AGAIN I DONT OWN THIS DONT ANGY THANKS LOVE UUU GO TO THEIR OWN PAGES 
