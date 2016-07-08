#!/bin/bash 

git checkout master
echo 'Current branch : master'

git pull
echo 'Pulling from master'

git checkout heroku
echo 'Current branch : heroku'

git merge master
echo 'Merge to master done'

git push heroku heroku:master
echo 'Push to heroku server done'

git checkout master
echo 'Current branch : master'
