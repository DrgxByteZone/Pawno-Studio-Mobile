/*  Pawn compiler
 *
 *  Routines to maintain a "text file" in memory, based on memory interface
 *  functions by faluco of the AMX Mod X team.
 *
 *  Copyright (c) ITB CompuPhase, 2003-2006
 *
 *  This software is provided 'as-is', without any express or implied warranty.
 *  In no event will the authors be held liable for any damages arising from the
 *  use of this software.
 *
 *  Permission is granted to anyone to use this software for any purpose,
 *  including commercial applications, and to alter it and redistribute it
 *  freely, subject to the following restrictions:
 *
 *  1. The origin of this software must not be misrepresented; you must not
 *     claim that you wrote the original software. If you use this software in
 *     a product, an acknowledgment in the product documentation would be
 *     appreciated but is not required.
 *
 *  2. Altered source versions must be plainly marked as such, and must not be
 *     misrepresented as being the original software.
 *
 *  3. This notice may not be removed or altered from any source distribution.
 *
 *  Version: $Id: scmemfil.c 3645 2006-09-24 16:00:29Z thiadmer $
 */

#include <assert.h>
#include <string.h>
#include "memfile.h"

#if defined FORTIFY
  #include <alloc/fortify.h>
#endif

typedef memfile_t MEMFILE;
#define tMEMFILE  1
#include "sc.h"


MEMFILE *mfcreate(const char *filename)
{
  return memfile_creat(filename, 4096);
}

void mfclose(MEMFILE *mf)
{
  memfile_destroy(mf);
}

int mfdump(MEMFILE *mf)
{
  FILE *fp;
  int okay;

  assert(mf!=NULL);
  /* create the file */
  fp=fopen(mf->name, "wb");
  if (fp==NULL)
    return 0;

  okay = (fwrite(mf->base, mf->usedoffs, 1, fp)==(size_t)mf->usedoffs);

  fclose(fp);
  return okay;
}

long mflength(const MEMFILE *mf)
{
  return mf->usedoffs;
}

long mfseek(MEMFILE *mf,long offset,int whence)
{
  long length;

  assert(mf!=NULL);
  if (mf->usedoffs == 0)
    return 0L;          /* early exit: not a single byte in the file */

  /* find the size of the memory file */
  length=mflength(mf);

  /* convert the offset to an absolute position */
  switch (whence) {
  case SEEK_SET:
    break;
  case SEEK_CUR:
    offset+=mf->offs;
    break;
  case SEEK_END:
    assert(offset<=0);
    offset+=length;
    break;
  } /* switch */

  /* clamp to the file length limit */
  if (offset<0)
    offset=0;
  else if (offset>length)
    offset=length;

  /* set new position and return it */
  memfile_seek(mf, offset);
  return offset;
}

unsigned int mfwrite(MEMFILE *mf,const unsigned char *buffer,unsigned int size)
{
  return (memfile_write(mf, buffer, size) ? size : 0);
}

unsigned int mfread(MEMFILE *mf,unsigned char *buffer,unsigned int size)
{
  return memfile_read(mf, buffer, size);
}

char *mfgets(MEMFILE *mf,char *string,unsigned int size)
{
  unsigned int avail, len;
  char *newline;

  if (mf == NULL || string == NULL || size <= 1)
    return NULL;
  if (mf->offs >= mf->usedoffs)
    return NULL;

  avail = (unsigned int)(mf->usedoffs - mf->offs);
  if (avail > size - 1)
    avail = size - 1;

  newline = (char *)memchr(mf->base + mf->offs, '\n', avail);
  if (newline != NULL) {
    len = (unsigned int)(newline - (mf->base + mf->offs)) + 1;
  } else {
    len = avail;
  }

  memcpy(string, mf->base + mf->offs, len);
  string[len] = '\0';
  mf->offs += len;
  return string;
}

int mfputs(MEMFILE *mf,const char *string)
{
  unsigned int written,length;

  assert(mf!=NULL);

  length=strlen(string);
  written=mfwrite(mf,(unsigned char *)string,length);
  return written==length;
}
