import argparse
import requests
import time
import datetime as dt
import math

###Credentials:


username = 'universityofantwerp_brits'
password = 'L4PsNZ0Xe8tpc'

#every time this function is called, the wind direction and speed are fetched from the meteomatics API
def api_get():
    realHour = dt.datetime.utcnow().hour
    realHour = realHour + 2
    now = dt.datetime.utcnow().replace(hour=realHour)

    if now.hour < 10:
        hourFormat = "0" + str(now.hour)
    else:
        hourFormat = str(now.hour)

    if now.minute < 10:
        minFormat = "0" + str(now.minute)
    else:
        minFormat = str(now.minute)

    if now.second < 10:
        secFormat = "0" + str(now.second)
    else:
        secFormat = str(now.second)

    timeFormatted = str(now.date()) + "T" + hourFormat + ":" + minFormat + ":" + secFormat + "Z"

    coordinatesFormatted = "51.395278,4.441111"

    url = "https://api.meteomatics.com/" + timeFormatted + "/wind_speed_10m:ms/" + coordinatesFormatted + "/csv"
    url2 = "https://api.meteomatics.com/" + timeFormatted + "/wind_dir_10m:d/" + coordinatesFormatted + "/csv"
    df_speed = requests.get(url, auth=(username, password))
    df_dir = requests.get(url2, auth=(username, password))

    cnt = 0
    charList = []
    i = 0
    for c in range(1, len(df_speed.content), 1):
        temp = chr(df_speed.content[c])
        if cnt == 2:
            charList.append(temp)
            i = i + 1
        if temp == ';':
            cnt = cnt + 1

    windSpeed = ''.join(charList)
    windSpeed = float(windSpeed) * 3.6
    charList2 = []
    cnt = 0
    for c in range(1, len(df_dir.content), 1):
        temp = chr(df_dir.content[c])
        if cnt == 2:
            charList2.append(temp)
            i = i + 1
        if temp == ';':
            cnt = cnt + 1

    windDir = float(''.join(charList2))

    returnVal = "$windspeed:" + str(windSpeed) + "$winddir:" + str(toRadians(windDir)) + '/' + 'n'
    print(returnVal)


def toRadians(windDir):
    Rdeg = windDir
    Rrad = math.radians(Rdeg)
    R = 0
    pi = math.pi

    if Rrad > pi:  # 3th & 4th quadrant => [0, -pi]
        R = Rrad - (2 * pi)
    else:  # 1th & 2nd quadrant => [0, pi]
        R = Rrad
    return R


if __name__ == "__main__":
    parser = argparse.ArgumentParser()
    parser.add_argument('--username', default=username)
    parser.add_argument('--password', default=password)
    arguments = parser.parse_args()

    username = arguments.username
    password = arguments.password

    while True:
        api_get()
        time.sleep(10)
