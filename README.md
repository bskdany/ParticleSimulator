<h1 align="center">Particle Simulator</h1>

<img src="https://github.com/bskdany/ParticleSimulator/blob/main/assets/ParticleSimulator.png" alt="particleSimulatorInItsFullForm">

[YouTube Video of the creation progress!](https://youtu.be/HubBLKdhpIE?si=-jT4HhaGMrCznJ3H)

## Description
ParticleSimulator is a multi threaded JavaFX application, it's compiled into native binaries thanks to [jDeploy](https://github.com/shannah/jdeploy). 

Executable are availables for Windows, Linux and MacOs (both x64 and arm). 

The program runs entirely on the CPU, the use of OpenGL for GPU integration was strongly considered but never implemented because it was too late in the project to rewrite everything again. 

## How to install
- Go to the [release page](https://github.com/bskdany/ParticleSimulator/releases/tag/main) and get the right file based on your operative system
- Follow the installation setup and run!

## How it's made
Each particle belongs to one of 7 species, each with it's own distinctive color.</br>
Each specie has a constant that determines how attracted or repulsed it is to a different species.

Example:
- Red is attracted to Pink 
- Pink is attracted to Orange
- Orange is repulsed by Red
- Red is attracted by Orange 

This is repeated for each species combination, with each constant holding value between -1 to +1.</br>
Those constant are represented via an Attraction Matrix.

The attraction constant (y_max) is plugged in the function below

<img src="https://github.com/bskdany/ParticleSimulator/blob/main/assets/attractionForceFunction.png" alt="aBeautifulPictureOfAFunction" width="80%" >

There is always a repulsion force if the particles are close enough, the value under which all the particles are repulsed is called Min Attraction Distance (x_max) , ranging from 0 to 1.

For each particle the cumulative force around it is calculated, normalized and then summed up to determine the velocity and thus it's next position.

For efficiency purposes the force is calculated only for the particles that fall in the range between the target particle and it's Max Attraction Distance. <br/>
By doing so you imply that if the distance between two particles is more that the Max Attraction Distance there will never be any force between them. <br/>
This optimization is necessary because it wasn't in place the number of calculations between particles would grow exponentially, which is beyond horrible.<br/>

Each frame (33ms) the particles are redrawn on a black JavaFX canvas, the result of which is artificial life.

I didn't like how the canvas had borders ( physics also hates the concept of an immovable wall ), so I implemented a wrapping system for particles and the underlying formulas for force calculation.<br/>
Oh boy it was nerve-wracking, I still think it's not perfect because apparently it's glitchy on Windows (not Linux tho idk why).

The rest of the project focused on UI and how stuff is displayed, like all the control sliders, buttons and data.

Finally I implemented a seed function that would encode the current attraction matrix and provide it as an Base64 encoded Gzipped string, two users can now share their favourite configuration between each other!

Another cool functionality is the timeline, every 200ms all the data is saved giving the possiblity to the user to go back in time and replay the simulation.

All of this because I wanted to learn more about Java
